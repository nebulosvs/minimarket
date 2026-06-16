package com.minimarket.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);
    private static final int MAX_EVENTS = 200;

    private final CopyOnWriteArrayList<SecurityAuditEvent> events = new CopyOnWriteArrayList<>();

    public void recordFailedLogin(String username, String channel, String clientIp) {
        register("FAILED_LOGIN", username, channel, clientIp,
                "Intento de autenticacion fallido");
    }

    public void recordSuccessfulLogin(String username, String channel, String clientIp) {
        register("SUCCESS_LOGIN", username, channel, clientIp,
                "Autenticacion exitosa");
    }

    public void recordSuspiciousActivity(String username, String channel, String clientIp, String detail) {
        register("SUSPICIOUS", username, channel, clientIp, detail);
        log.warn("Actividad sospechosa [{}] usuario={} ip={} detalle={}", channel, username, clientIp, detail);
    }

    public List<SecurityAuditEvent> recentEvents() {
        List<SecurityAuditEvent> snapshot = new ArrayList<>(events);
        Collections.reverse(snapshot);
        return snapshot.stream().limit(50).toList();
    }

    private void register(String type, String username, String channel, String clientIp, String detail) {
        SecurityAuditEvent event = new SecurityAuditEvent(
                Instant.now(),
                type,
                username,
                channel,
                clientIp,
                detail
        );
        events.add(event);
        while (events.size() > MAX_EVENTS) {
            events.remove(0);
        }
        if ("FAILED_LOGIN".equals(type) || "SUSPICIOUS".equals(type)) {
            log.warn("Auditoria seguridad [{}] usuario={} canal={} ip={} detalle={}",
                    type, username, channel, clientIp, detail);
        } else {
            log.info("Auditoria seguridad [{}] usuario={} canal={} ip={}",
                    type, username, channel, clientIp);
        }
    }
}
