package com.sentinel.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.CreateStudentRequest;
import com.sentinel.ledger.dto.StudentDto;
import com.sentinel.ledger.dto.StudentHistoryDto;
import com.sentinel.ledger.dto.UpdateStudentRequest;
import com.sentinel.ledger.entity.Credential;
import com.sentinel.ledger.entity.Institution;
import com.sentinel.ledger.entity.Student;
import com.sentinel.ledger.entity.StudentHistory;
import com.sentinel.ledger.enums.AdmissionCategory;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.enums.CredentialStatus;
import com.sentinel.ledger.enums.VerificationStatus;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.CredentialRepository;
import com.sentinel.ledger.repository.InstitutionRepository;
import com.sentinel.ledger.repository.StudentHistoryRepository;
import com.sentinel.ledger.repository.StudentRepository;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentHistoryRepository historyRepository;
    private final InstitutionRepository institutionRepository;
    private final CredentialRepository credentialRepository;
    private final InstitutionSecurityEvaluator securityEvaluator;
    private final DuplicateDetectionService duplicateService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public StudentService(StudentRepository studentRepository,
                          StudentHistoryRepository historyRepository,
                          InstitutionRepository institutionRepository,
                          CredentialRepository credentialRepository,
                          InstitutionSecurityEvaluator securityEvaluator,
                          DuplicateDetectionService duplicateService,
                          AuditService auditService,
                          ObjectMapper objectMapper) {
        this.studentRepository = studentRepository;
        this.historyRepository = historyRepository;
        this.institutionRepository = institutionRepository;
        this.credentialRepository = credentialRepository;
        this.securityEvaluator = securityEvaluator;
        this.duplicateService = duplicateService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public Page<StudentDto> getStudents(Long institutionId, String program, String academicYear,
                                        AdmissionCategory category, AdmissionStatus status,
                                        Boolean credentialIssued, String query, Pageable pageable) {
        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);

        Specification<Student> spec = (root, q, cb) -> {
            var predicate = cb.conjunction();
            if (effectiveInstId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("institution").get("id"), effectiveInstId));
            }
            if (program != null && !program.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("program"), program.trim()));
            }
            if (academicYear != null && !academicYear.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("academicYear"), academicYear.trim()));
            }
            if (category != null) {
                predicate = cb.and(predicate, cb.equal(root.get("admissionCategory"), category));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("admissionStatus"), status));
            }
            if (credentialIssued != null) {
                Subquery<Long> subquery = q.subquery(Long.class);
                Root<Credential> credRoot = subquery.from(Credential.class);
                subquery.select(credRoot.get("student").get("id"))
                        .where(cb.and(
                                cb.equal(credRoot.get("student").get("id"), root.get("id")),
                                cb.equal(credRoot.get("status"), CredentialStatus.VALID)
                        ));
                if (credentialIssued) {
                    predicate = cb.and(predicate, cb.exists(subquery));
                } else {
                    predicate = cb.and(predicate, cb.not(cb.exists(subquery)));
                }
            }
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.trim().toLowerCase() + "%";
                var nameLike = cb.like(cb.lower(root.get("studentName")), searchPattern);
                var apaarLike = cb.like(cb.lower(root.get("syntheticApaarId")), searchPattern);
                var enrollLike = cb.like(cb.lower(root.get("enrollmentNumber")), searchPattern);
                predicate = cb.and(predicate, cb.or(nameLike, apaarLike, enrollLike));
            }
            return predicate;
        };

        return studentRepository.findAll(spec, pageable).map(this::toDto);
    }

    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        securityEvaluator.validateInstitutionAccess(student.getInstitution().getId());
        return toDto(student);
    }

    @Transactional
    public StudentDto createStudent(CreateStudentRequest request) {
        Long targetInstitutionId = securityEvaluator.resolveTargetInstitutionId(request.getInstitutionId());
        Institution institution = institutionRepository.findById(targetInstitutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + targetInstitutionId));

        String normalizedApaar = duplicateService.normalizeSyntheticId(request.getSyntheticApaarId());

        Student student = new Student(
                institution,
                normalizedApaar,
                request.getEnrollmentNumber().trim(),
                request.getStudentName().trim(),
                request.getProgram().trim(),
                request.getAcademicYear().trim(),
                request.getAdmissionYear(),
                request.getAdmissionCategory(),
                request.getAdmissionStatus() != null ? request.getAdmissionStatus() : AdmissionStatus.ADMITTED,
                request.getVerificationStatus() != null ? request.getVerificationStatus() : VerificationStatus.VERIFIED,
                request.getGender() != null ? request.getGender() : "UNSPECIFIED",
                request.getDob()
        );

        Student saved = studentRepository.save(student);

        // Check cross-institution duplicates
        List<Student> otherMatches = duplicateService.findCrossInstitutionMatches(normalizedApaar, institution.getId());
        if (!otherMatches.isEmpty()) {
            saved.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
            studentRepository.save(saved);
            for (Student match : otherMatches) {
                duplicateService.createDuplicateCase(saved, match);
            }
        }

        auditService.log("STUDENT_CREATED", "STUDENT", saved.getId().toString(), AuditOutcome.SUCCESS,
                "Student record created for: " + saved.getStudentName() + " (" + saved.getSyntheticApaarId() + ")");

        return toDto(saved);
    }

    @Transactional
    public StudentDto updateStudent(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        securityEvaluator.validateInstitutionAccess(student.getInstitution().getId());

        UserPrincipal currentUser = securityEvaluator.getCurrentUser();
        String oldValueJson;
        try {
            oldValueJson = objectMapper.writeValueAsString(toDto(student));
        } catch (Exception e) {
            oldValueJson = "{}";
        }

        if (request.getStudentName() != null) student.setStudentName(request.getStudentName().trim());
        if (request.getProgram() != null) student.setProgram(request.getProgram().trim());
        if (request.getAcademicYear() != null) student.setAcademicYear(request.getAcademicYear().trim());
        if (request.getAdmissionYear() != null) student.setAdmissionYear(request.getAdmissionYear());
        if (request.getAdmissionCategory() != null) student.setAdmissionCategory(request.getAdmissionCategory());
        if (request.getAdmissionStatus() != null) student.setAdmissionStatus(request.getAdmissionStatus());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getDob() != null) student.setDob(request.getDob());

        Student updated = studentRepository.save(student);

        String newValueJson;
        try {
            newValueJson = objectMapper.writeValueAsString(toDto(updated));
        } catch (Exception e) {
            newValueJson = "{}";
        }

        // Save Revision History
        StudentHistory history = new StudentHistory(
                updated,
                currentUser.getUsername(),
                "UPDATE",
                oldValueJson,
                newValueJson
        );
        historyRepository.save(history);

        auditService.log("STUDENT_UPDATED", "STUDENT", updated.getId().toString(), AuditOutcome.SUCCESS,
                "Student record updated by " + currentUser.getUsername() + ". Reason: " + request.getUpdateReason());

        return toDto(updated);
    }

    public List<StudentHistoryDto> getStudentHistory(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        securityEvaluator.validateInstitutionAccess(student.getInstitution().getId());

        return historyRepository.findByStudentIdOrderByTimestampDesc(studentId).stream()
                .map(h -> {
                    StudentHistoryDto dto = new StudentHistoryDto();
                    dto.setId(h.getId());
                    dto.setStudentId(h.getStudent().getId());
                    dto.setModifiedBy(h.getModifiedBy());
                    dto.setChangeType(h.getChangeType());
                    dto.setOldValueJson(h.getOldValueJson());
                    dto.setNewValueJson(h.getNewValueJson());
                    dto.setTimestamp(h.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public StudentDto toDto(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setInstitutionId(s.getInstitution().getId());
        dto.setInstitutionName(s.getInstitution().getName());
        dto.setInstitutionCode(s.getInstitution().getCode());
        dto.setSyntheticApaarId(s.getSyntheticApaarId());
        dto.setEnrollmentNumber(s.getEnrollmentNumber());
        dto.setStudentName(s.getStudentName());
        dto.setProgram(s.getProgram());
        dto.setAcademicYear(s.getAcademicYear());
        dto.setAdmissionYear(s.getAdmissionYear());
        dto.setAdmissionCategory(s.getAdmissionCategory());
        dto.setAdmissionStatus(s.getAdmissionStatus());
        dto.setVerificationStatus(s.getVerificationStatus());
        dto.setGender(s.getGender());
        dto.setDob(s.getDob());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());

        // Check active credential status from registry
        credentialRepository.findTopByStudentIdOrderByCreatedAtDesc(s.getId()).ifPresent(cred -> {
            if (cred.getStatus() == CredentialStatus.VALID) {
                dto.setCredentialIssued(true);
                dto.setCredentialId(cred.getCredentialId());
                dto.setCredentialIssueDate(cred.getIssueDate());
            }
        });

        return dto;
    }
}
