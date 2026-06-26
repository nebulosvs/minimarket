package com.minimarket.entity;

import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.audit.SecurityAuditService;
import com.minimarket.security.jwt.JwtService;
import com.minimarket.security.model.CustomUserDetails;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.service.AuthService;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioAutenticacionTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityAuditService securityAuditService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioValido;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuarioValido = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        loginRequest = new LoginRequest();
        loginRequest.setUsername("cliente");
        loginRequest.setPassword("Cliente123!");
    }

    @Test
    void login_credencialesValidas_retornaToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuarioValido));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("token.jwt.valido");

        String token = authService.login(loginRequest);

        assertNotNull(token);
        assertEquals("token.jwt.valido", token);
        verify(securityAuditService).recordSuccessfulLogin("cliente", "JWT-LOCAL", "unknown");
    }

    @Test
    void login_passwordIncorrecta_lanzaBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(securityAuditService).recordFailedLogin("cliente", "JWT-LOCAL", "unknown");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_usuarioInexistenteTrasAutenticacion_lanzaBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_usernameConCaracteresSospechosos_lanzaBadCredentialsException() {
        loginRequest.setUsername("admin' OR '1'='1");

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(authenticationManager, never()).authenticate(any());
        verify(securityAuditService).recordSuspiciousActivity(
                "admin' or '1'='1",
                "JWT-LOCAL",
                "unknown",
                "Caracteres no permitidos en nombre de usuario"
        );
    }

    @Test
    void login_usernameConEspacios_normalizaMinusculas() {
        loginRequest.setUsername("  Cliente  ");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuarioValido));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("token");

        authService.login(loginRequest);

        verify(usuarioRepository).findByUsername("cliente");
    }

    @Test
    void login_usuarioSinPasswordEnRequest_fallaAutenticacion() {
        loginRequest.setPassword("claveIncorrecta");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
