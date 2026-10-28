package com.sentinel.ledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.IssueCredentialRequest;
import com.sentinel.ledger.dto.ResolveCaseRequest;
import com.sentinel.ledger.dto.UpdateProfileRequest;
import com.sentinel.ledger.dto.VerifyCredentialRequest;
import com.sentinel.ledger.entity.*;
import com.sentinel.ledger.enums.*;
import com.sentinel.ledger.exception.AccessDeniedException;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.repository.*;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import com.sentinel.ledger.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RegistrarEnhancementsTest {

    private InstitutionSecurityEvaluator securityEvaluator;
    private CredentialRepository credentialRepository;
    private StudentRepository studentRepository;
    private CryptoService cryptoService;
    private AuditService auditService;
    private ObjectMapper objectMapper;
    private CredentialService credentialService;

    private ImportBatchRepository batchRepository;
    private ImportRowRepository rowRepository;
    private DuplicateDetectionService duplicateService;
    private ImportService importService;

    private ReviewCaseRepository reviewCaseRepository;
    private ReviewService reviewService;

    private UserRepository userRepository;
    private UserService userService;

    private Institution instDelhi;
    private Institution instMaha;
    private User registrarDelhi;

    @BeforeEach
    void setUp() {
        securityEvaluator = new InstitutionSecurityEvaluator();
        credentialRepository = mock(CredentialRepository.class);
        studentRepository = mock(StudentRepository.class);
        cryptoService = new CryptoService("aicte-test-key", "SHA256withRSA");
        AuditLogRepository auditLogRepo = mock(AuditLogRepository.class);
        auditService = new AuditService(auditLogRepo);
        objectMapper = new ObjectMapper();

        credentialService = new CredentialService(
                credentialRepository, studentRepository, cryptoService,
                securityEvaluator, auditService, objectMapper
        );

        batchRepository = mock(ImportBatchRepository.class);
        rowRepository = mock(ImportRowRepository.class);
        reviewCaseRepository = mock(ReviewCaseRepository.class);
        duplicateService = new DuplicateDetectionService(studentRepository, reviewCaseRepository);
        ProgramIntakeRepository intakeRepo = mock(ProgramIntakeRepository.class);
        InstitutionRepository instRepo = mock(InstitutionRepository.class);

        importService = new ImportService(
                batchRepository, rowRepository, instRepo, studentRepository,
                intakeRepo, duplicateService, securityEvaluator, auditService, objectMapper
        );

        reviewService = new ReviewService(
                reviewCaseRepository, securityEvaluator, auditService, objectMapper
        );

        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository, securityEvaluator, auditService);

        instDelhi = new Institution("Delhi Tech", "DEL-101", "Delhi", "New Delhi", "APPROVED", 120, "delhi@test.com", "123");
        instDelhi.setId(1L);

        instMaha = new Institution("Maha Tech", "MAH-202", "Maha", "Pune", "APPROVED", 120, "maha@test.com", "456");
        instMaha.setId(2L);

        registrarDelhi = new User("reg_delhi", "pass", "Dr. Delhi Registrar", "reg@delhi.ac.in", Role.COLLEGE_REGISTRAR, instDelhi);
        registrarDelhi.setId(10L);

        // Set security context as registrar of instDelhi (ID 1)
        UserPrincipal principal = new UserPrincipal(registrarDelhi);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("Registrar cannot access student record belonging to another institution (IDOR check)")
    void testRegistrarCannotAccessOtherInstitutionStudent() {
        assertThrows(AccessDeniedException.class, () -> {
            securityEvaluator.validateInstitutionAccess(2L);
        });
    }

    @Test
    @DisplayName("Registrar can access student record belonging to own institution")
    void testRegistrarCanAccessOwnInstitutionStudent() {
        assertDoesNotThrow(() -> {
            securityEvaluator.validateInstitutionAccess(1L);
        });
    }

    @Test
    @DisplayName("Credential issuance rejected if student status is PENDING_REVIEW")
    void testIssueCredentialRejectsPendingReviewStudent() {
        Student pendingStudent = new Student(instDelhi, "APAAR-001", "ENR-001", "John Doe",
                "CSE", "2024-2025", 2024, AdmissionCategory.GENERAL,
                AdmissionStatus.ADMITTED, VerificationStatus.PENDING_REVIEW, "MALE", LocalDate.of(2003, 1, 1));
        pendingStudent.setId(100L);

        when(studentRepository.findById(100L)).thenReturn(Optional.of(pendingStudent));

        IssueCredentialRequest req = new IssueCredentialRequest();
        req.setStudentId(100L);
        req.setQualification("B.Tech CSE");
        req.setCompletionYear(2024);

        assertThrows(BadRequestException.class, () -> credentialService.issueCredential(req));
    }

    @Test
    @DisplayName("Credential issuance rejected if student already has active valid credential")
    void testIssueCredentialRejectsAlreadyIssued() {
        Student verifiedStudent = new Student(instDelhi, "APAAR-002", "ENR-002", "Jane Doe",
                "CSE", "2024-2025", 2024, AdmissionCategory.GENERAL,
                AdmissionStatus.ADMITTED, VerificationStatus.VERIFIED, "FEMALE", LocalDate.of(2003, 1, 1));
        verifiedStudent.setId(101L);

        when(studentRepository.findById(101L)).thenReturn(Optional.of(verifiedStudent));
        when(credentialRepository.existsByStudentIdAndStatus(101L, CredentialStatus.VALID)).thenReturn(true);

        IssueCredentialRequest req = new IssueCredentialRequest();
        req.setStudentId(101L);
        req.setQualification("B.Tech CSE");
        req.setCompletionYear(2024);

        assertThrows(BadRequestException.class, () -> credentialService.issueCredential(req));
    }

    @Test
    @DisplayName("Credential verification flags EXPIRED if completion year has elapsed")
    void testCredentialVerificationExpiryByGraduationYear() throws Exception {
        Map<String, Object> claims = new TreeMap<>();
        claims.put("credentialId", "AICTE-CRED-TEST");
        claims.put("syntheticSubjectRef", "APAAR-003");
        claims.put("institutionCode", instDelhi.getCode());
        claims.put("qualification", "B.Tech");
        claims.put("completionYear", 2020);
        claims.put("issueDate", LocalDate.of(2020, 6, 1).toString());
        String canonicalPayload = objectMapper.writeValueAsString(claims);
        String sig = cryptoService.sign(canonicalPayload);
        String pubKey = cryptoService.getPublicKeyPem();

        Credential expiredCred = new Credential(
                "AICTE-CRED-TEST", null, "APAAR-003", instDelhi, "B.Tech", 2020, "9.0", "Delhi Tech",
                LocalDate.of(2020, 6, 1), CredentialStatus.VALID, "key-1", "SHA256withRSA", sig, pubKey
        );

        when(credentialRepository.findByCredentialId("AICTE-CRED-TEST")).thenReturn(Optional.of(expiredCred));

        VerifyCredentialRequest req = new VerifyCredentialRequest();
        req.setCredentialId("AICTE-CRED-TEST");

        var result = credentialService.verifyCredential(req);
        assertFalse(result.isValid());
        assertEquals("EXPIRED", result.getCredentialStatus());
    }

    @Test
    @DisplayName("Registrar cannot verify credential belonging to another institution")
    void testRegistrarCannotVerifyCrossInstitutionCredential() {
        Credential otherInstCred = new Credential(
                "AICTE-CRED-OTHER", null, "APAAR-004", instMaha, "B.Tech", 2026, "9.0", "Maha Tech",
                LocalDate.of(2024, 6, 1), CredentialStatus.VALID, "key-1", "SHA256withRSA", "sig", "pubkey"
        );

        when(credentialRepository.findByCredentialId("AICTE-CRED-OTHER")).thenReturn(Optional.of(otherInstCred));

        VerifyCredentialRequest req = new VerifyCredentialRequest();
        req.setCredentialId("AICTE-CRED-OTHER");

        var result = credentialService.verifyCredential(req);
        assertFalse(result.isValid());
        assertEquals("INSTITUTION_MISMATCH", result.getCredentialStatus());
    }

    @Test
    @DisplayName("Authorized registrar receives full student name for own institution credential")
    void testAuthorizedRegistrarReceivesFullName() throws Exception {
        Student student = new Student(instDelhi, "APAAR-005", "ENR-005", "Puveesh Keshav",
                "CSE", "2024-2025", 2024, AdmissionCategory.GENERAL,
                AdmissionStatus.ADMITTED, VerificationStatus.VERIFIED, "MALE", LocalDate.of(2003, 1, 1));

        Map<String, Object> claims = new TreeMap<>();
        claims.put("credentialId", "AICTE-CRED-OWN");
        claims.put("syntheticSubjectRef", "APAAR-005");
        claims.put("institutionCode", instDelhi.getCode());
        claims.put("qualification", "B.Tech");
        claims.put("completionYear", 2026);
        claims.put("issueDate", LocalDate.of(2024, 6, 1).toString());
        String canonicalPayload = objectMapper.writeValueAsString(claims);
        String sig = cryptoService.sign(canonicalPayload);
        String pubKey = cryptoService.getPublicKeyPem();

        Credential ownCred = new Credential(
                "AICTE-CRED-OWN", student, "APAAR-005", instDelhi, "B.Tech", 2026, "9.0", "Delhi Tech",
                LocalDate.of(2024, 6, 1), CredentialStatus.VALID, "key-1", "SHA256withRSA", sig, pubKey
        );

        when(credentialRepository.findByCredentialId("AICTE-CRED-OWN")).thenReturn(Optional.of(ownCred));

        VerifyCredentialRequest req = new VerifyCredentialRequest();
        req.setCredentialId("AICTE-CRED-OWN");

        var result = credentialService.verifyCredential(req);
        assertTrue(result.isValid());
        assertEquals("Puveesh Keshav", result.getStudentName());
    }

    @Test
    @DisplayName("Staged uncommitted import batch can be cancelled successfully")
    void testCancelStagedBatch() {
        ImportBatch batch = new ImportBatch(instDelhi, "reg_delhi", "students.csv", 1024L);
        batch.setStatus(ImportBatchStatus.PREVIEWED);
        batch.setId(50L);

        when(batchRepository.findById(50L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ImportBatch.class))).thenAnswer(i -> i.getArgument(0));

        var dto = importService.cancelBatch(50L);
        assertEquals(ImportBatchStatus.CANCELLED, dto.getStatus());
    }

    @Test
    @DisplayName("Already committed import batch cannot be cancelled")
    void testCannotCancelCommittedBatch() {
        ImportBatch batch = new ImportBatch(instDelhi, "reg_delhi", "students.csv", 1024L);
        batch.setStatus(ImportBatchStatus.COMMITTED);
        batch.setId(51L);

        when(batchRepository.findById(51L)).thenReturn(Optional.of(batch));

        assertThrows(BadRequestException.class, () -> importService.cancelBatch(51L));
    }

    @Test
    @DisplayName("Review case decision can be amended and history is recorded")
    void testReviewCaseDecisionAmendmentRecordsHistory() {
        ReviewCase reviewCase = new ReviewCase("REV-001", CaseType.DUPLICATE_CANDIDATE, CaseStatus.OPEN, Severity.HIGH, instDelhi, null, "{}");
        reviewCase.setId(70L);

        when(reviewCaseRepository.findById(70L)).thenReturn(Optional.of(reviewCase));
        when(reviewCaseRepository.save(any(ReviewCase.class))).thenAnswer(i -> i.getArgument(0));

        ResolveCaseRequest req = new ResolveCaseRequest();
        req.setStatus(CaseStatus.CONFIRMED_DUPLICATE);
        req.setResolutionReason("Confirmed candidate duplicate admission with other college");

        var res = reviewService.resolveCase(70L, req);
        assertEquals(CaseStatus.CONFIRMED_DUPLICATE, res.getStatus());
        assertNotNull(res.getDecisionHistoryJson());
        assertTrue(res.getDecisionHistoryJson().contains("OPEN"));
        assertTrue(res.getDecisionHistoryJson().contains("CONFIRMED_DUPLICATE"));
    }

    @Test
    @DisplayName("Review case can be safely dismissed with mandatory reason")
    void testReviewCaseDismissal() {
        ReviewCase reviewCase = new ReviewCase("REV-002", CaseType.INVALID_DATA, CaseStatus.OPEN, Severity.LOW, instDelhi, null, "{}");
        reviewCase.setId(71L);

        when(reviewCaseRepository.findById(71L)).thenReturn(Optional.of(reviewCase));
        when(reviewCaseRepository.save(any(ReviewCase.class))).thenAnswer(i -> i.getArgument(0));

        var res = reviewService.dismissCase(71L, "Data format verified and found valid under AICTE exemption");
        assertEquals(CaseStatus.DISMISSED, res.getStatus());
        assertTrue(res.getDecisionHistoryJson().contains("DISMISSED"));
    }

    @Test
    @DisplayName("Registrar can update own full name only, cannot tamper with role or institution")
    void testProfileUpdateOnlyChangesFullName() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(registrarDelhi));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest("Dr. Rajesh Kumar Sharma");
        var res = userService.updateProfile(req);

        assertEquals("Dr. Rajesh Kumar Sharma", res.getFullName());
        assertEquals("reg_delhi", res.getUsername());
        assertEquals(Role.COLLEGE_REGISTRAR, res.getRole());
        assertEquals(1L, res.getInstitutionId());
    }
}
