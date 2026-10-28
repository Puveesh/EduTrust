package com.sentinel.ledger.service;

import com.sentinel.ledger.entity.Institution;
import com.sentinel.ledger.entity.ReviewCase;
import com.sentinel.ledger.entity.Student;
import com.sentinel.ledger.enums.CaseStatus;
import com.sentinel.ledger.enums.CaseType;
import com.sentinel.ledger.enums.Severity;
import com.sentinel.ledger.repository.ReviewCaseRepository;
import com.sentinel.ledger.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DuplicateDetectionService {

    private final StudentRepository studentRepository;
    private final ReviewCaseRepository reviewCaseRepository;

    public DuplicateDetectionService(StudentRepository studentRepository, ReviewCaseRepository reviewCaseRepository) {
        this.studentRepository = studentRepository;
        this.reviewCaseRepository = reviewCaseRepository;
    }

    public String normalizeSyntheticId(String apaarId) {
        if (apaarId == null) return "";
        return apaarId.trim().toUpperCase().replaceAll("\\s+", "");
    }

    /**
     * Checks if a synthetic APAAR ID is already enrolled in another institution.
     */
    public List<Student> findCrossInstitutionMatches(String syntheticApaarId, Long currentInstitutionId) {
        String normalized = normalizeSyntheticId(syntheticApaarId);
        if (normalized.isEmpty()) return List.of();
        return studentRepository.findDuplicatesInOtherInstitutions(normalized, currentInstitutionId);
    }

    /**
     * Creates an OPEN review case when a potential cross-institution duplicate candidate is detected.
     */
    public ReviewCase createDuplicateCase(Student targetStudent, Student matchedStudent) {
        String caseNumber = "REV-DUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String evidenceJson = String.format(
                "{\"matchedStudentId\": %d, \"matchedStudentName\": \"%s\", \"matchedInstitutionId\": %d, \"matchedInstitutionName\": \"%s\", \"syntheticApaarId\": \"%s\", \"note\": \"Possible duplicate: same synthetic student reference appears in another institution.\"}",
                matchedStudent.getId(),
                matchedStudent.getStudentName(),
                matchedStudent.getInstitution().getId(),
                matchedStudent.getInstitution().getName(),
                targetStudent.getSyntheticApaarId()
        );

        ReviewCase reviewCase = new ReviewCase(
                caseNumber,
                CaseType.DUPLICATE_CANDIDATE,
                CaseStatus.OPEN,
                Severity.HIGH,
                targetStudent.getInstitution(),
                targetStudent,
                evidenceJson
        );

        return reviewCaseRepository.save(reviewCase);
    }
}
