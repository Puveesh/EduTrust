package com.sentinel.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.*;
import com.sentinel.ledger.entity.*;
import com.sentinel.ledger.enums.*;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.*;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImportService {

    private final ImportBatchRepository batchRepository;
    private final ImportRowRepository rowRepository;
    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;
    private final ProgramIntakeRepository intakeRepository;
    private final DuplicateDetectionService duplicateService;
    private final InstitutionSecurityEvaluator securityEvaluator;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public ImportService(ImportBatchRepository batchRepository,
                         ImportRowRepository rowRepository,
                         InstitutionRepository institutionRepository,
                         StudentRepository studentRepository,
                         ProgramIntakeRepository intakeRepository,
                         DuplicateDetectionService duplicateService,
                         InstitutionSecurityEvaluator securityEvaluator,
                         AuditService auditService,
                         ObjectMapper objectMapper) {
        this.batchRepository = batchRepository;
        this.rowRepository = rowRepository;
        this.institutionRepository = institutionRepository;
        this.studentRepository = studentRepository;
        this.intakeRepository = intakeRepository;
        this.duplicateService = duplicateService;
        this.securityEvaluator = securityEvaluator;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public String generateCsvTemplate() {
        return "syntheticApaarId,studentName,enrollmentNumber,program,academicYear,admissionYear,admissionCategory,gender\n" +
               "APAAR-SYN-2024-8801,Aarav Sharma,ENR-2024-8801,Computer Science and Engineering,2024-2025,2024,GENERAL,MALE\n" +
               "APAAR-SYN-2024-8802,Priya Patel,ENR-2024-8802,Information Technology,2024-2025,2024,OBC,FEMALE\n" +
               "APAAR-SYN-2024-8803,Rohan Verma,ENR-2024-8803,Mechanical Engineering,2024-2025,2024,SC,MALE\n";
    }

    @Transactional
    public ImportPreviewResponse processCsvUpload(MultipartFile file, Long targetInstitutionId) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded CSV file is empty");
        }

        Long effectiveInstitutionId = securityEvaluator.resolveTargetInstitutionId(targetInstitutionId);
        Institution institution = institutionRepository.findById(effectiveInstitutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + effectiveInstitutionId));

        UserPrincipal currentUser = securityEvaluator.getCurrentUser();

        ImportBatch batch = new ImportBatch(
                institution,
                currentUser.getUsername(),
                file.getOriginalFilename(),
                file.getSize()
        );
        batch = batchRepository.save(batch);

        List<ImportRow> rowsToSave = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;
        int duplicateCount = 0;
        Set<String> batchApaarSet = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).setIgnoreHeaderCase(true).build())) {

            int rowNumber = 1;
            for (CSVRecord record : csvParser) {
                String rawApaar = record.isMapped("syntheticApaarId") ? record.get("syntheticApaarId") : "";
                String name = record.isMapped("studentName") ? record.get("studentName") : "";
                String program = record.isMapped("program") ? record.get("program") : "";
                String academicYear = record.isMapped("academicYear") ? record.get("academicYear") : "";
                String admissionYearStr = record.isMapped("admissionYear") ? record.get("admissionYear") : "";
                String category = record.isMapped("admissionCategory") ? record.get("admissionCategory") : "";

                ImportRow row = new ImportRow();
                row.setBatch(batch);
                row.setRowNumber(rowNumber++);
                row.setSyntheticApaarId(rawApaar);
                row.setStudentName(name);
                row.setProgram(program);
                row.setAcademicYear(academicYear);
                row.setAdmissionCategory(category);

                Integer admissionYear = null;
                try {
                    admissionYear = Integer.parseInt(admissionYearStr);
                    row.setAdmissionYear(admissionYear);
                } catch (NumberFormatException ignored) {}

                // Validation
                List<String> errors = new ArrayList<>();
                if (rawApaar == null || rawApaar.trim().isEmpty()) {
                    errors.add("Missing synthetic APAAR reference");
                }
                if (name == null || name.trim().isEmpty()) {
                    errors.add("Missing student name");
                }
                if (program == null || program.trim().isEmpty()) {
                    errors.add("Missing program/course");
                }
                if (academicYear == null || academicYear.trim().isEmpty()) {
                    errors.add("Missing academic year");
                }
                if (admissionYear == null || admissionYear < 2000 || admissionYear > 2030) {
                    errors.add("Invalid admission year (must be between 2000-2030)");
                }

                if (category == null || category.trim().isEmpty()) {
                    errors.add("Missing admission category");
                } else {
                    try {
                        AdmissionCategory.valueOf(category.toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        errors.add("Invalid admission category: " + category);
                    }
                }

                // Check intra-batch duplicate
                String normalizedApaar = duplicateService.normalizeSyntheticId(rawApaar);
                if (!normalizedApaar.isEmpty()) {
                    if (batchApaarSet.contains(normalizedApaar)) {
                        errors.add("Duplicate synthetic identifier detected within this import batch");
                    } else {
                        batchApaarSet.add(normalizedApaar);
                    }
                }

                // Check cross-institution duplicates
                List<Student> crossMatches = duplicateService.findCrossInstitutionMatches(normalizedApaar, institution.getId());
                boolean isDuplicate = !crossMatches.isEmpty();

                if (!errors.isEmpty()) {
                    row.setStatus(ImportRowStatus.INVALID);
                    row.setValidationErrorsJson(objectMapper.writeValueAsString(errors));
                    invalidCount++;
                } else if (isDuplicate) {
                    row.setStatus(ImportRowStatus.DUPLICATE_CANDIDATE);
                    row.setDuplicateExplanation("Possible duplicate: Same synthetic reference appears in institution: " + crossMatches.get(0).getInstitution().getName());
                    duplicateCount++;
                } else {
                    row.setStatus(ImportRowStatus.VALID);
                    validCount++;
                }

                rowsToSave.add(row);
            }

        } catch (Exception e) {
            batch.setStatus(ImportBatchStatus.FAILED);
            batchRepository.save(batch);
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }

        rowRepository.saveAll(rowsToSave);

        batch.setTotalRows(rowsToSave.size());
        batch.setValidRows(validCount);
        batch.setInvalidRows(invalidCount);
        batch.setDuplicateCandidates(duplicateCount);
        batch.setStatus(invalidCount > 0 ? ImportBatchStatus.NEEDS_CORRECTION : ImportBatchStatus.PREVIEWED);
        ImportBatch savedBatch = batchRepository.save(batch);

        auditService.log("IMPORT_PREVIEWED", "IMPORT_BATCH", savedBatch.getId().toString(), AuditOutcome.SUCCESS,
                "Import batch uploaded: " + savedBatch.getFileName() + " (" + savedBatch.getTotalRows() + " rows)");

        return new ImportPreviewResponse(
                toBatchDto(savedBatch),
                rowsToSave.stream().map(this::toRowDto).toList(),
                invalidCount > 0,
                duplicateCount > 0,
                0
        );
    }

    public Page<ImportBatchDto> getBatches(Long institutionId, Pageable pageable) {
        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);
        if (effectiveInstId != null) {
            return batchRepository.findByInstitutionIdOrderByCreatedAtDesc(effectiveInstId, pageable).map(this::toBatchDto);
        }
        return batchRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toBatchDto);
    }

    public ImportBatchDto getBatchById(Long batchId) {
        ImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
        securityEvaluator.validateInstitutionAccess(batch.getInstitution().getId());
        return toBatchDto(batch);
    }

    public Page<ImportRowDto> getBatchRows(Long batchId, ImportRowStatus status, Pageable pageable) {
        ImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
        securityEvaluator.validateInstitutionAccess(batch.getInstitution().getId());

        if (status != null) {
            return rowRepository.findByBatchIdAndStatus(batchId, status, pageable).map(this::toRowDto);
        }
        return rowRepository.findByBatchId(batchId, pageable).map(this::toRowDto);
    }

    @Transactional
    public ImportRowDto updateBatchRow(Long batchId, Long rowId, UpdateImportRowRequest request) {
        ImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
        securityEvaluator.validateInstitutionAccess(batch.getInstitution().getId());

        if (batch.getStatus() == ImportBatchStatus.COMMITTED) {
            throw new BadRequestException("Cannot edit rows in an already committed batch");
        }

        ImportRow row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Import row not found: " + rowId));

        if (!row.getBatch().getId().equals(batchId)) {
            throw new BadRequestException("Row does not belong to specified batch");
        }

        if (request.getStatus() == ImportRowStatus.EXCLUDED) {
            row.setStatus(ImportRowStatus.EXCLUDED);
            row.setEdited(true);
            return toRowDto(rowRepository.save(row));
        }

        if (request.getSyntheticApaarId() != null) row.setSyntheticApaarId(request.getSyntheticApaarId());
        if (request.getStudentName() != null) row.setStudentName(request.getStudentName());
        if (request.getProgram() != null) row.setProgram(request.getProgram());
        if (request.getAcademicYear() != null) row.setAcademicYear(request.getAcademicYear());
        if (request.getAdmissionYear() != null) row.setAdmissionYear(request.getAdmissionYear());
        if (request.getAdmissionCategory() != null) row.setAdmissionCategory(request.getAdmissionCategory());

        // Revalidate row
        List<String> errors = new ArrayList<>();
        if (row.getSyntheticApaarId() == null || row.getSyntheticApaarId().trim().isEmpty()) errors.add("Missing synthetic APAAR reference");
        if (row.getStudentName() == null || row.getStudentName().trim().isEmpty()) errors.add("Missing student name");
        if (row.getProgram() == null || row.getProgram().trim().isEmpty()) errors.add("Missing program");
        if (row.getAcademicYear() == null || row.getAcademicYear().trim().isEmpty()) errors.add("Missing academic year");
        if (row.getAdmissionYear() == null || row.getAdmissionYear() < 2000) errors.add("Invalid admission year");

        try {
            AdmissionCategory.valueOf(row.getAdmissionCategory().toUpperCase().trim());
        } catch (Exception e) {
            errors.add("Invalid admission category: " + row.getAdmissionCategory());
        }

        row.setEdited(true);

        try {
            if (!errors.isEmpty()) {
                row.setStatus(ImportRowStatus.INVALID);
                row.setValidationErrorsJson(objectMapper.writeValueAsString(errors));
            } else {
                List<Student> crossMatches = duplicateService.findCrossInstitutionMatches(row.getSyntheticApaarId(), batch.getInstitution().getId());
                if (!crossMatches.isEmpty()) {
                    row.setStatus(ImportRowStatus.DUPLICATE_CANDIDATE);
                    row.setDuplicateExplanation("Possible duplicate in institution: " + crossMatches.get(0).getInstitution().getName());
                } else {
                    row.setStatus(ImportRowStatus.VALID);
                    row.setValidationErrorsJson(null);
                    row.setDuplicateExplanation(null);
                }
            }
        } catch (Exception ignored) {}

        ImportRow savedRow = rowRepository.save(row);

        // Update batch counts
        long invalidCount = rowRepository.countByBatchIdAndStatus(batchId, ImportRowStatus.INVALID);
        long validCount = rowRepository.countByBatchIdAndStatus(batchId, ImportRowStatus.VALID);
        long dupCount = rowRepository.countByBatchIdAndStatus(batchId, ImportRowStatus.DUPLICATE_CANDIDATE);
        long excludedCount = rowRepository.countByBatchIdAndStatus(batchId, ImportRowStatus.EXCLUDED);

        batch.setInvalidRows((int) invalidCount);
        batch.setValidRows((int) validCount);
        batch.setDuplicateCandidates((int) dupCount);
        batch.setExcludedRows((int) excludedCount);
        batch.setStatus(invalidCount == 0 ? ImportBatchStatus.READY_TO_COMMIT : ImportBatchStatus.NEEDS_CORRECTION);
        batchRepository.save(batch);

        return toRowDto(savedRow);
    }

    @Transactional
    public ImportBatchDto cancelBatch(Long batchId) {
        ImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
        securityEvaluator.validateInstitutionAccess(batch.getInstitution().getId());

        if (batch.getStatus() == ImportBatchStatus.COMMITTED) {
            throw new BadRequestException("Cannot cancel an already committed batch. Staged cancellation is only allowed for uncommitted imports.");
        }

        if (batch.getStatus() == ImportBatchStatus.CANCELLED) {
            throw new BadRequestException("Batch is already cancelled.");
        }

        batch.setStatus(ImportBatchStatus.CANCELLED);
        ImportBatch saved = batchRepository.save(batch);

        // Exclude all remaining rows
        List<ImportRow> rows = rowRepository.findByBatchIdOrderByRowNumberAsc(batchId);
        for (ImportRow r : rows) {
            r.setStatus(ImportRowStatus.EXCLUDED);
        }
        rowRepository.saveAll(rows);

        UserPrincipal user = securityEvaluator.getCurrentUser();
        auditService.log("IMPORT_BATCH_CANCELLED", "IMPORT_BATCH", saved.getId().toString(), AuditOutcome.SUCCESS,
                "Import batch #" + saved.getId() + " (" + saved.getFileName() + ") cancelled by " + user.getUsername());

        return toBatchDto(saved);
    }

    @Transactional
    public CommitBatchResponse commitBatch(Long batchId) {
        ImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Import batch not found: " + batchId));
        securityEvaluator.validateInstitutionAccess(batch.getInstitution().getId());

        if (batch.getStatus() == ImportBatchStatus.COMMITTED) {
            throw new BadRequestException("Batch has already been committed! Cannot commit twice.");
        }

        if (batch.getStatus() == ImportBatchStatus.CANCELLED) {
            throw new BadRequestException("This batch was cancelled and is no longer eligible for commit.");
        }

        List<ImportRow> rows = rowRepository.findByBatchIdOrderByRowNumberAsc(batchId);
        List<Student> studentsToSave = new ArrayList<>();
        List<String> flaggedCaseNumbers = new ArrayList<>();
        int committedCount = 0;
        int excludedCount = 0;

        for (ImportRow row : rows) {
            if (row.getStatus() == ImportRowStatus.EXCLUDED) {
                excludedCount++;
                continue;
            }
            if (row.getStatus() == ImportRowStatus.INVALID) {
                throw new BadRequestException("Cannot commit batch containing invalid rows. Please correct or exclude row #" + row.getRowNumber());
            }

            // Create student record
            AdmissionCategory category = AdmissionCategory.GENERAL;
            try {
                category = AdmissionCategory.valueOf(row.getAdmissionCategory().toUpperCase().trim());
            } catch (Exception ignored) {}

            String enrollmentNo = "ENR-" + batch.getInstitution().getCode() + "-" + row.getRowNumber() + "-" + System.currentTimeMillis() % 10000;
            Student student = new Student(
                    batch.getInstitution(),
                    duplicateService.normalizeSyntheticId(row.getSyntheticApaarId()),
                    enrollmentNo,
                    row.getStudentName(),
                    row.getProgram(),
                    row.getAcademicYear(),
                    row.getAdmissionYear() != null ? row.getAdmissionYear() : 2024,
                    category,
                    AdmissionStatus.ADMITTED,
                    row.getStatus() == ImportRowStatus.DUPLICATE_CANDIDATE ? VerificationStatus.PENDING_REVIEW : VerificationStatus.VERIFIED,
                    "UNSPECIFIED",
                    null
            );

            Student savedStudent = studentRepository.save(student);

            if (row.getStatus() == ImportRowStatus.DUPLICATE_CANDIDATE) {
                List<Student> otherMatches = duplicateService.findCrossInstitutionMatches(row.getSyntheticApaarId(), batch.getInstitution().getId());
                if (!otherMatches.isEmpty()) {
                    ReviewCase reviewCase = duplicateService.createDuplicateCase(savedStudent, otherMatches.get(0));
                    flaggedCaseNumbers.add(reviewCase.getCaseNumber());
                }
            }

            row.setStatus(ImportRowStatus.IMPORTED);
            rowRepository.save(row);
            committedCount++;
        }

        batch.setStatus(ImportBatchStatus.COMMITTED);
        batch.setCommittedAt(LocalDateTime.now());
        batchRepository.save(batch);

        auditService.log("IMPORT_COMMITTED", "IMPORT_BATCH", batch.getId().toString(), AuditOutcome.SUCCESS,
                "Batch committed successfully. " + committedCount + " students created, " + flaggedCaseNumbers.size() + " review cases opened.");

        return new CommitBatchResponse(
                batch.getId(),
                "COMMITTED",
                committedCount,
                excludedCount,
                flaggedCaseNumbers.size(),
                "Import batch committed successfully with " + committedCount + " student records added.",
                flaggedCaseNumbers
        );
    }

    public ImportBatchDto toBatchDto(ImportBatch b) {
        ImportBatchDto dto = new ImportBatchDto();
        dto.setId(b.getId());
        dto.setInstitutionId(b.getInstitution().getId());
        dto.setInstitutionName(b.getInstitution().getName());
        dto.setUploadedBy(b.getUploadedBy());
        dto.setFileName(b.getFileName());
        dto.setFileSize(b.getFileSize());
        dto.setTotalRows(b.getTotalRows());
        dto.setValidRows(b.getValidRows());
        dto.setInvalidRows(b.getInvalidRows());
        dto.setDuplicateCandidates(b.getDuplicateCandidates());
        dto.setExcludedRows(b.getExcludedRows());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setCommittedAt(b.getCommittedAt());
        return dto;
    }

    public ImportRowDto toRowDto(ImportRow r) {
        ImportRowDto dto = new ImportRowDto();
        dto.setId(r.getId());
        dto.setBatchId(r.getBatch().getId());
        dto.setRowNumber(r.getRowNumber());
        dto.setSyntheticApaarId(r.getSyntheticApaarId());
        dto.setStudentName(r.getStudentName());
        dto.setProgram(r.getProgram());
        dto.setAcademicYear(r.getAcademicYear());
        dto.setAdmissionYear(r.getAdmissionYear());
        dto.setAdmissionCategory(r.getAdmissionCategory());
        dto.setStatus(r.getStatus());
        dto.setValidationErrorsJson(r.getValidationErrorsJson());
        dto.setDuplicateExplanation(r.getDuplicateExplanation());
        dto.setEdited(r.isEdited());
        return dto;
    }
}
