package com.sentinel.ledger.service;

import com.sentinel.ledger.dto.AuditLogDto;
import com.sentinel.ledger.dto.DashboardSummaryDto;
import com.sentinel.ledger.dto.ReviewCaseDto;
import com.sentinel.ledger.enums.CaseStatus;
import com.sentinel.ledger.enums.CaseType;
import com.sentinel.ledger.enums.Role;
import com.sentinel.ledger.repository.*;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final ImportBatchRepository batchRepository;
    private final ReviewCaseRepository reviewCaseRepository;
    private final AuditLogRepository auditLogRepository;
    private final InstitutionSecurityEvaluator securityEvaluator;
    private final ReviewService reviewService;
    private final IntakeComplianceService intakeComplianceService;

    public DashboardService(StudentRepository studentRepository,
                            InstitutionRepository institutionRepository,
                            ImportBatchRepository batchRepository,
                            ReviewCaseRepository reviewCaseRepository,
                            AuditLogRepository auditLogRepository,
                            InstitutionSecurityEvaluator securityEvaluator,
                            ReviewService reviewService,
                            IntakeComplianceService intakeComplianceService) {
        this.studentRepository = studentRepository;
        this.institutionRepository = institutionRepository;
        this.batchRepository = batchRepository;
        this.reviewCaseRepository = reviewCaseRepository;
        this.auditLogRepository = auditLogRepository;
        this.securityEvaluator = securityEvaluator;
        this.reviewService = reviewService;
        this.intakeComplianceService = intakeComplianceService;
    }

    public DashboardSummaryDto getSummary(Long institutionId) {
        return getSummary(institutionId, null);
    }

    public DashboardSummaryDto getSummary(Long institutionId, String academicYear) {
        UserPrincipal user = securityEvaluator.getCurrentUser();
        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);

        DashboardSummaryDto dto = new DashboardSummaryDto();

        if (effectiveInstId != null) {
            dto.setTotalStudents(studentRepository.countByInstitutionId(effectiveInstId));
            dto.setTotalInstitutions(1);
            dto.setRecordsAwaitingReview(reviewCaseRepository.countByInstitutionIdAndStatus(effectiveInstId, CaseStatus.OPEN));
            dto.setTotalImportBatches(batchRepository.findByInstitutionIdOrderByCreatedAtDesc(effectiveInstId).size());
            dto.setOpenDuplicateCases(reviewCaseRepository.countByInstitutionIdAndCaseTypeAndStatus(
                    effectiveInstId, CaseType.DUPLICATE_CANDIDATE, CaseStatus.OPEN));
            dto.setIntakeViolationsCount(reviewCaseRepository.countByInstitutionIdAndCaseType(
                    effectiveInstId, CaseType.INTAKE_VIOLATION));

            // Scoped groupings
            Map<String, Long> byInst = new HashMap<>();
            institutionRepository.findById(effectiveInstId).ifPresent(i -> byInst.put(i.getName(), dto.getTotalStudents()));
            dto.setStudentsByInstitution(byInst);

            Map<String, Long> byYear = new HashMap<>();
            for (Object[] row : studentRepository.countByAcademicYearGroupForInstitution(effectiveInstId)) {
                byYear.put((String) row[0], (Long) row[1]);
            }
            dto.setStudentsByAcademicYear(byYear);

            Map<String, Long> byStatus = new HashMap<>();
            for (Object[] row : studentRepository.countByStatusGroupForInstitution(effectiveInstId)) {
                byStatus.put(row[0].toString(), (Long) row[1]);
            }
            dto.setStudentsByStatus(byStatus);

            Map<String, Long> byProgram = new HashMap<>();
            for (Object[] row : studentRepository.countByProgramGroupForInstitution(effectiveInstId)) {
                byProgram.put((String) row[0], (Long) row[1]);
            }
            dto.setStudentsByProgram(byProgram);

            // Scoped recent audit logs
            List<AuditLogDto> recentLogs = auditLogRepository.findTop20ByInstitutionIdOrderByTimestampDesc(effectiveInstId).stream().map(log -> {
                AuditLogDto l = new AuditLogDto();
                l.setId(log.getId());
                l.setUserId(log.getUserId());
                l.setUsername(log.getUsername());
                l.setAction(log.getAction());
                l.setEntityType(log.getEntityType());
                l.setEntityId(log.getEntityId());
                l.setTimestamp(log.getTimestamp());
                l.setOutcome(log.getOutcome());
                l.setReason(log.getReason());
                l.setIpAddress(log.getIpAddress());
                l.setCorrelationId(log.getCorrelationId());
                return l;
            }).collect(Collectors.toList());
            dto.setRecentAuditLogs(recentLogs);

        } else {
            dto.setTotalStudents(studentRepository.count());
            dto.setTotalInstitutions(institutionRepository.count());
            dto.setRecordsAwaitingReview(reviewCaseRepository.countByStatus(CaseStatus.OPEN));
            dto.setTotalImportBatches(batchRepository.count());
            dto.setOpenDuplicateCases(reviewCaseRepository.countOpenDuplicateCases());
            dto.setIntakeViolationsCount(reviewCaseRepository.countByCaseType(CaseType.INTAKE_VIOLATION));

            // Global groupings
            Map<String, Long> byInst = new HashMap<>();
            for (Object[] row : studentRepository.countByInstitutionGroup()) {
                byInst.put((String) row[0], (Long) row[1]);
            }
            dto.setStudentsByInstitution(byInst);

            Map<String, Long> byYear = new HashMap<>();
            for (Object[] row : studentRepository.countByAcademicYearGroup()) {
                byYear.put((String) row[0], (Long) row[1]);
            }
            dto.setStudentsByAcademicYear(byYear);

            Map<String, Long> byStatus = new HashMap<>();
            for (Object[] row : studentRepository.countByStatusGroup()) {
                byStatus.put(row[0].toString(), (Long) row[1]);
            }
            dto.setStudentsByStatus(byStatus);

            Map<String, Long> byProgram = new HashMap<>();
            for (Object[] row : studentRepository.countByProgramGroup()) {
                byProgram.put((String) row[0], (Long) row[1]);
            }
            dto.setStudentsByProgram(byProgram);

            // Global recent audit logs
            List<AuditLogDto> recentLogs = auditLogRepository.findTop20ByOrderByTimestampDesc().stream().map(log -> {
                AuditLogDto l = new AuditLogDto();
                l.setId(log.getId());
                l.setUserId(log.getUserId());
                l.setUsername(log.getUsername());
                l.setAction(log.getAction());
                l.setEntityType(log.getEntityType());
                l.setEntityId(log.getEntityId());
                l.setTimestamp(log.getTimestamp());
                l.setOutcome(log.getOutcome());
                l.setReason(log.getReason());
                l.setIpAddress(log.getIpAddress());
                l.setCorrelationId(log.getCorrelationId());
                return l;
            }).collect(Collectors.toList());
            dto.setRecentAuditLogs(recentLogs);
        }

        // Program-wise Intake Overview for selected/current academic year
        dto.setProgramIntakeOverview(intakeComplianceService.getIntakeCompliance(effectiveInstId, academicYear));

        // Recent review cases
        dto.setRecentReviewCases(reviewService.getCases(null, null, effectiveInstId, PageRequest.of(0, 5)).getContent());

        return dto;
    }
}
