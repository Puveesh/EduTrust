package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.AuditLogDto;
import com.sentinel.ledger.entity.AuditLog;
import com.sentinel.ledger.repository.AuditLogRepository;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Logging", description = "Immutable audit event trail for security, data modifications, and compliance actions")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final InstitutionSecurityEvaluator securityEvaluator;

    public AuditController(AuditLogRepository auditLogRepository, InstitutionSecurityEvaluator securityEvaluator) {
        this.auditLogRepository = auditLogRepository;
        this.securityEvaluator = securityEvaluator;
    }

    @GetMapping("/logs")
    @Operation(summary = "Query system audit logs with filters and pagination")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);

        Specification<AuditLog> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (effectiveInstId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("institutionId"), effectiveInstId));
            }
            if (action != null && !action.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("action"), action.trim()));
            }
            if (username != null && !username.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%"));
            }
            return predicate;
        };

        Page<AuditLogDto> results = auditLogRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")))
                .map(log -> {
                    AuditLogDto dto = new AuditLogDto();
                    dto.setId(log.getId());
                    dto.setUserId(log.getUserId());
                    dto.setUsername(log.getUsername());
                    dto.setInstitutionId(log.getInstitutionId());
                    dto.setAction(log.getAction());
                    dto.setEntityType(log.getEntityType());
                    dto.setEntityId(log.getEntityId());
                    dto.setTimestamp(log.getTimestamp());
                    dto.setOutcome(log.getOutcome());
                    dto.setReason(log.getReason());
                    dto.setIpAddress(log.getIpAddress());
                    dto.setCorrelationId(log.getCorrelationId());
                    return dto;
                });

        return ResponseEntity.ok(results);
    }
}
