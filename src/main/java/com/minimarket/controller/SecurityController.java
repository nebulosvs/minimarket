package com.minimarket.controller;

import com.minimarket.security.audit.SecurityAuditEvent;
import com.minimarket.security.audit.SecurityAuditService;
import com.minimarket.security.oauth.OAuth2IdaasStatusService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityAuditService securityAuditService;
    private final OAuth2IdaasStatusService oAuth2IdaasStatusService;

    public SecurityController(
            SecurityAuditService securityAuditService,
            OAuth2IdaasStatusService oAuth2IdaasStatusService
    ) {
        this.securityAuditService = securityAuditService;
        this.oAuth2IdaasStatusService = oAuth2IdaasStatusService;
    }

    @GetMapping("/oauth2/status")
    public Map<String, Object> oauth2Status() {
        return oAuth2IdaasStatusService.status();
    }

    @PreAuthorize("hasRole('GERENTE')")
    @GetMapping("/audit/events")
    public List<SecurityAuditEvent> auditEvents() {
        return securityAuditService.recentEvents();
    }

    @GetMapping("/info")
    public Map<String, Object> securityInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "MiniMarket Plus API");
        info.put("authentication", List.of("JWT-local", "LDAP", "IDaaS-OIDC"));
        info.put("authorization", "RBAC con Spring Security");
        info.put("oauth2", oAuth2IdaasStatusService.status());
        return info;
    }
}
