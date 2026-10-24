package com.sentinel.ledger.entity;

import com.sentinel.ledger.enums.AuditOutcome;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "username"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "institution_id")
    private Long institutionId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditOutcome outcome = AuditOutcome.SUCCESS;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog() {}

    public AuditLog(Long userId, String username, Long institutionId, String action, String entityType, String entityId, AuditOutcome outcome, String reason, String ipAddress, String correlationId) {
        this.userId = userId;
        this.username = username;
        this.institutionId = institutionId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
        this.reason = reason;
        this.ipAddress = ipAddress;
        this.correlationId = correlationId;
    }

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
