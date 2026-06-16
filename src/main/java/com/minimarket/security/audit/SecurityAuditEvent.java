package com.minimarket.security.audit;

import java.time.Instant;

public record SecurityAuditEvent(
        Instant timestamp,
        String type,
        String username,
        String channel,
        String clientIp,
        String detail
) {
}
