package com.minimarket.security.service;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.audit.SecurityAuditService;
import com.minimarket.security.jwt.JwtService;
import com.minimarket.security.model.CustomUserDetails;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.model.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityAuditService securityAuditService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            SecurityAuditService securityAuditService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.securityAuditService = securityAuditService;
    }

    public String register(RegisterRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        Set<Rol> roles = resolveRoles(request.getRoles());
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(defaultIfBlank(request.getNombre(), capitalize(username)));
        usuario.setApellido(defaultIfBlank(request.getApellido(), "Usuario"));
        usuario.setEmail(defaultIfBlank(request.getEmail(), username + "@minimarket.cl"));
        usuario.setDireccion(defaultIfBlank(request.getDireccion(), "Sin direccion registrada"));
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRoles(roles);

        Usuario savedUser = usuarioRepository.save(usuario);
        return jwtService.generateToken(new CustomUserDetails(savedUser));
    }

    public String login(LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String clientIp = resolveClientIp();

        if (containsSuspiciousCharacters(username)) {
            securityAuditService.recordSuspiciousActivity(
                    username, "JWT-LOCAL", clientIp, "Caracteres no permitidos en nombre de usuario"
            );
            throw new BadCredentialsException("Credenciales invalidas");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
        } catch (Exception ex) {
            securityAuditService.recordFailedLogin(username, "JWT-LOCAL", clientIp);
            throw new BadCredentialsException("Credenciales invalidas");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        securityAuditService.recordSuccessfulLogin(username, "JWT-LOCAL", clientIp);
        return jwtService.generateToken(new CustomUserDetails(usuario));
    }

    private boolean containsSuspiciousCharacters(String value) {
        return value.contains("'") || value.contains("\"") || value.contains(";")
                || value.contains("--") || value.contains("<") || value.contains(">");
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

    private Set<Rol> resolveRoles(Set<String> roleNames) {
        Set<String> requestedRoles = (roleNames == null || roleNames.isEmpty())
                ? Set.of("CLIENTE")
                : roleNames;

        Set<Rol> roles = new HashSet<>();
        for (String roleName : requestedRoles) {
            String normalized = roleName.trim().toUpperCase();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            Rol rol = rolRepository.findByNombre(normalized)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + roleName));
            roles.add(rol);
        }
        return roles;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
