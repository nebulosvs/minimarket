package com.minimarket.security.ldap;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.audit.SecurityAuditService;
import com.minimarket.security.jwt.JwtService;
import com.minimarket.security.model.CustomUserDetails;
import com.minimarket.security.model.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "app.security.ldap.enabled", havingValue = "true")
public class LdapAuthService {

    private final LdapAuthenticationProvider ldapAuthenticationProvider;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityAuditService securityAuditService;

    public LdapAuthService(
            LdapAuthenticationProvider ldapAuthenticationProvider,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SecurityAuditService securityAuditService
    ) {
        this.ldapAuthenticationProvider = ldapAuthenticationProvider;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityAuditService = securityAuditService;
    }

    public String login(LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String clientIp = resolveClientIp();

        try {
            Authentication authentication = ldapAuthenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            Usuario usuario = provisionLocalUser(username, authentication);
            securityAuditService.recordSuccessfulLogin(username, "LDAP", clientIp);
            return jwtService.generateToken(new CustomUserDetails(usuario));
        } catch (Exception ex) {
            securityAuditService.recordFailedLogin(username, "LDAP", clientIp);
            throw new BadCredentialsException("Credenciales LDAP invalidas");
        }
    }

    private Usuario provisionLocalUser(String username, Authentication authentication) {
        return usuarioRepository.findByUsername(username).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setNombre(capitalize(username.replace('.', ' ')));
            usuario.setApellido("LDAP");
            usuario.setEmail(username + "@minimarket.cl");
            usuario.setDireccion("Usuario sincronizado desde LDAP");
            usuario.setPassword(passwordEncoder.encode("LDAP_SYNCED_USER"));
            usuario.setRoles(mapRoles(authentication));
            return usuarioRepository.save(usuario);
        });
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private Set<Rol> mapRoles(Authentication authentication) {
        Set<String> roleNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        if (roleNames.isEmpty()) {
            roleNames.add("EMPLEADO");
        }

        Set<Rol> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Rol rol = rolRepository.findByNombre(roleName)
                    .orElseGet(() -> {
                        Rol newRole = new Rol();
                        newRole.setNombre(roleName);
                        return rolRepository.save(newRole);
                    });
            roles.add(rol);
        }
        return roles;
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
