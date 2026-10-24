package com.sentinel.ledger.audit;

import com.sentinel.ledger.entity.AuditLog;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.repository.AuditLogRepository;
import com.sentinel.ledger.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityType, String entityId, AuditOutcome outcome, String reason) {
        Long userId = null;
        String username = "ANONYMOUS";
        Long institutionId = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getId();
            username = principal.getUsername();
            institutionId = principal.getInstitutionId();
        }

        String ipAddress = "127.0.0.1";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String xForwardedFor = req.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    ipAddress = xForwardedFor.split(",")[0].trim();
                } else {
                    ipAddress = req.getRemoteAddr();
                }
            }
        } catch (Exception ignored) {}

        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        AuditLog auditLog = new AuditLog(userId, username, institutionId, action, entityType, entityId, outcome, reason, ipAddress, correlationId);
        auditLogRepository.save(auditLog);
    }
}
