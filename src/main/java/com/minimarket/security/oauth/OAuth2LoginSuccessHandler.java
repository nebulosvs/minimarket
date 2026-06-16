package com.minimarket.security.oauth;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.audit.SecurityAuditService;
import com.minimarket.security.jwt.JwtService;
import com.minimarket.security.model.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.security.oauth2.client.enabled", havingValue = "true")
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityAuditService securityAuditService;

    @Value("${app.security.oauth2.success-redirect:http://localhost:3000/auth/callback}")
    private String successRedirect;

    public OAuth2LoginSuccessHandler(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SecurityAuditService securityAuditService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityAuditService = securityAuditService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String username = resolveUsername(oidcUser);
        Usuario usuario = provisionUser(username);
        String token = jwtService.generateToken(new CustomUserDetails(usuario));
        securityAuditService.recordSuccessfulLogin(username, "IDaaS-OIDC", request.getRemoteAddr());

        String redirectUrl = successRedirect
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }

    private String resolveUsername(OidcUser oidcUser) {
        if (oidcUser.getPreferredUsername() != null) {
            return oidcUser.getPreferredUsername().toLowerCase();
        }
        if (oidcUser.getEmail() != null) {
            return oidcUser.getEmail().toLowerCase();
        }
        return oidcUser.getSubject();
    }

    private Usuario provisionUser(String username) {
        return usuarioRepository.findByUsername(username).orElseGet(() -> {
            Rol cliente = rolRepository.findByNombre("CLIENTE").orElseGet(() -> {
                Rol role = new Rol();
                role.setNombre("CLIENTE");
                return rolRepository.save(role);
            });
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode("IDaaS_SYNCED_USER"));
            usuario.setRoles(new HashSet<>(Set.of(cliente)));
            return usuarioRepository.save(usuario);
        });
    }
}
