package com.sentinel.ledger.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.ResolveCaseRequest;
import com.sentinel.ledger.dto.ReviewCaseDto;
import com.sentinel.ledger.entity.ReviewCase;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.enums.CaseStatus;
import com.sentinel.ledger.enums.CaseType;
import com.sentinel.ledger.enums.Role;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.ReviewCaseRepository;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final ReviewCaseRepository reviewCaseRepository;
    private final InstitutionSecurityEvaluator securityEvaluator;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public ReviewService(ReviewCaseRepository reviewCaseRepository,
                         InstitutionSecurityEvaluator securityEvaluator,
                         AuditService auditService,
                         ObjectMapper objectMapper) {
        this.reviewCaseRepository = reviewCaseRepository;
        this.securityEvaluator = securityEvaluator;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public Page<ReviewCaseDto> getCases(CaseType caseType, CaseStatus status, Long institutionId, Pageable pageable) {
        UserPrincipal user = securityEvaluator.getCurrentUser();
        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);

        Specification<ReviewCase> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (effectiveInstId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("institution").get("id"), effectiveInstId));
            }
            if (caseType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("caseType"), caseType));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };

        return reviewCaseRepository.findAll(spec, pageable).map(this::toDto);
    }

    public ReviewCaseDto getCaseById(Long id) {
        ReviewCase reviewCase = reviewCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review case not found with id: " + id));

        if (reviewCase.getInstitution() != null) {
            securityEvaluator.validateInstitutionAccess(reviewCase.getInstitution().getId());
        }

        return toDto(reviewCase);
    }

    @Transactional
    public ReviewCaseDto resolveCase(Long id, ResolveCaseRequest request) {
        ReviewCase reviewCase = reviewCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review case not found with id: " + id));

        if (reviewCase.getInstitution() != null) {
            securityEvaluator.validateInstitutionAccess(reviewCase.getInstitution().getId());
        }

        if (request.getResolutionReason() == null || request.getResolutionReason().trim().isEmpty()) {
            throw new BadRequestException("Mandatory resolution reason must be provided");
        }

        UserPrincipal currentUser = securityEvaluator.getCurrentUser();

        // Append to decision history log
        List<Map<String, Object>> historyList = new ArrayList<>();
        if (reviewCase.getDecisionHistoryJson() != null && !reviewCase.getDecisionHistoryJson().isBlank()) {
            try {
                historyList = objectMapper.readValue(reviewCase.getDecisionHistoryJson(), new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception ignored) {}
        }

        Map<String, Object> historyEntry = new LinkedHashMap<>();
        historyEntry.put("timestamp", LocalDateTime.now().toString());
        historyEntry.put("actingUser", currentUser.getUsername());
        historyEntry.put("previousStatus", reviewCase.getStatus().name());
        historyEntry.put("newStatus", request.getStatus().name());
        historyEntry.put("previousReason", reviewCase.getResolutionReason());
        historyEntry.put("newReason", request.getResolutionReason().trim());
        historyList.add(historyEntry);

        try {
            reviewCase.setDecisionHistoryJson(objectMapper.writeValueAsString(historyList));
        } catch (Exception ignored) {}

        reviewCase.setStatus(request.getStatus());
        reviewCase.setResolutionReason(request.getResolutionReason().trim());
        reviewCase.setResolvedBy(currentUser.getUsername());
        reviewCase.setResolvedAt(LocalDateTime.now());

        ReviewCase saved = reviewCaseRepository.save(reviewCase);

        auditService.log("REVIEW_CASE_RESOLVED", "REVIEW_CASE", saved.getCaseNumber(),
                AuditOutcome.SUCCESS, "Status updated to " + request.getStatus() + ". Reason: " + request.getResolutionReason());

        return toDto(saved);
    }

    @Transactional
    public ReviewCaseDto dismissCase(Long id, String reason) {
        ReviewCase reviewCase = reviewCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review case not found with id: " + id));

        if (reviewCase.getInstitution() != null) {
            securityEvaluator.validateInstitutionAccess(reviewCase.getInstitution().getId());
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Mandatory reason is required to dismiss or withdraw a review case.");
        }

        if (reviewCase.getStatus() == CaseStatus.DISMISSED) {
            throw new BadRequestException("Case has already been dismissed.");
        }

        UserPrincipal currentUser = securityEvaluator.getCurrentUser();

        // Append to decision history
        List<Map<String, Object>> historyList = new ArrayList<>();
        if (reviewCase.getDecisionHistoryJson() != null && !reviewCase.getDecisionHistoryJson().isBlank()) {
            try {
                historyList = objectMapper.readValue(reviewCase.getDecisionHistoryJson(), new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception ignored) {}
        }

        Map<String, Object> historyEntry = new LinkedHashMap<>();
        historyEntry.put("timestamp", LocalDateTime.now().toString());
        historyEntry.put("actingUser", currentUser.getUsername());
        historyEntry.put("previousStatus", reviewCase.getStatus().name());
        historyEntry.put("newStatus", CaseStatus.DISMISSED.name());
        historyEntry.put("previousReason", reviewCase.getResolutionReason());
        historyEntry.put("newReason", reason.trim());
        historyList.add(historyEntry);

        try {
            reviewCase.setDecisionHistoryJson(objectMapper.writeValueAsString(historyList));
        } catch (Exception ignored) {}

        reviewCase.setStatus(CaseStatus.DISMISSED);
        reviewCase.setResolutionReason(reason.trim());
        reviewCase.setResolvedBy(currentUser.getUsername());
        reviewCase.setResolvedAt(LocalDateTime.now());

        ReviewCase saved = reviewCaseRepository.save(reviewCase);

        auditService.log("REVIEW_CASE_DISMISSED", "REVIEW_CASE", saved.getCaseNumber(),
                AuditOutcome.SUCCESS, "Case dismissed / marked false positive by " + currentUser.getUsername() + ". Reason: " + reason);

        return toDto(saved);
    }

    private ReviewCaseDto toDto(ReviewCase rc) {
        ReviewCaseDto dto = new ReviewCaseDto();
        dto.setId(rc.getId());
        dto.setCaseNumber(rc.getCaseNumber());
        dto.setCaseType(rc.getCaseType());
        dto.setStatus(rc.getStatus());
        dto.setSeverity(rc.getSeverity());
        if (rc.getInstitution() != null) {
            dto.setInstitutionId(rc.getInstitution().getId());
            dto.setInstitutionName(rc.getInstitution().getName());
        }
        if (rc.getTargetStudent() != null) {
            dto.setTargetStudentId(rc.getTargetStudent().getId());
            dto.setTargetStudentName(rc.getTargetStudent().getStudentName());
        }
        dto.setEvidenceJson(rc.getEvidenceJson());
        dto.setResolutionReason(rc.getResolutionReason());
        dto.setDecisionHistoryJson(rc.getDecisionHistoryJson());
        dto.setResolvedBy(rc.getResolvedBy());
        dto.setCreatedAt(rc.getCreatedAt());
        dto.setUpdatedAt(rc.getUpdatedAt());
        dto.setResolvedAt(rc.getResolvedAt());
        return dto;
    }
}
