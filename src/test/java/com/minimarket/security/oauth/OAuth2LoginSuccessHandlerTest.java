package com.minimarket.security.oauth;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.audit.SecurityAuditService;
import com.minimarket.security.jwt.JwtService;
import com.minimarket.security.model.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SecurityAuditService securityAuditService;
    @Mock
    private Authentication authentication;

    private OAuth2LoginSuccessHandler handler;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new OAuth2LoginSuccessHandler(
                usuarioRepository, rolRepository, passwordEncoder, jwtService, securityAuditService
        );
        ReflectionTestUtils.setField(handler, "successRedirect", "http://localhost:3000/callback");
        response = new MockHttpServletResponse();
    }

    @Test
    void onAuthenticationSuccess_redirigeConToken() throws Exception {
        OidcUser oidcUser = oidcUser("usuario.oauth", null);
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario.oauth");
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(usuarioRepository.findByUsername("usuario.oauth")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token");

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        assertTrue(response.getRedirectedUrl().contains("token=jwt-token"));
        verify(securityAuditService).recordSuccessfulLogin(eq("usuario.oauth"), eq("IDaaS-OIDC"), any());
    }

    @Test
    void onAuthenticationSuccess_provisionaUsuarioNuevo() throws Exception {
        OidcUser oidcUser = oidcUser(null, "nuevo@minimarket.cl");
        Rol cliente = new Rol();
        cliente.setNombre("CLIENTE");
        Usuario nuevo = new Usuario();
        nuevo.setUsername("nuevo@minimarket.cl");

        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(usuarioRepository.findByUsername("nuevo@minimarket.cl")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevo);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-nuevo");

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        verify(usuarioRepository).save(any(Usuario.class));
        assertTrue(response.getRedirectedUrl().contains("jwt-nuevo"));
    }

    private OidcUser oidcUser(String preferredUsername, String email) {
        java.util.HashMap<String, Object> claims = new java.util.HashMap<>();
        claims.put("sub", "sub-123");
        if (preferredUsername != null) {
            claims.put("preferred_username", preferredUsername);
        }
        if (email != null) {
            claims.put("email", email);
        }
        OidcIdToken idToken = new OidcIdToken(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                claims
        );
        return new DefaultOidcUser(Set.of(), idToken);
    }
}
