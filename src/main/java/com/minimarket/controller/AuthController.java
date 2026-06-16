package com.minimarket.controller;

import com.minimarket.security.ldap.LdapAuthService;
import com.minimarket.security.model.AuthResponse;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.model.RegisterRequest;
import com.minimarket.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ObjectProvider<LdapAuthService> ldapAuthService;

    public AuthController(AuthService authService, ObjectProvider<LdapAuthService> ldapAuthService) {
        this.authService = authService;
        this.ldapAuthService = ldapAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/ldap/login")
    public ResponseEntity<AuthResponse> ldapLogin(@Valid @RequestBody LoginRequest request) {
        LdapAuthService ldapService = ldapAuthService.getIfAvailable();
        if (ldapService == null) {
            throw new IllegalStateException("La autenticacion LDAP no esta habilitada");
        }
        String token = ldapService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
