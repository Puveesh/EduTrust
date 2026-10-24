package com.sentinel.ledger.dto;

import com.sentinel.ledger.enums.AuditOutcome;
import java.time.LocalDateTime;

public class AuditLogDto {
    private Long id;
    private Long userId;
    private String username;
    private Long institutionId;
    private String action;
    private String entityType;
    private String entityId;
    private LocalDateTime timestamp;
    private AuditOutcome outcome;
    private String reason;
    private String ipAddress;
    private String correlationId;

    public AuditLogDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public AuditOutcome getOutcome() { return outcome; }
    public void setOutcome(AuditOutcome outcome) { this.outcome = outcome; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
