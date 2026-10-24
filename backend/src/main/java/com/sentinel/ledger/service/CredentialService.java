package com.sentinel.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.*;
import com.sentinel.ledger.entity.Credential;
import com.sentinel.ledger.entity.Student;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.enums.CredentialStatus;
import com.sentinel.ledger.enums.Role;
import com.sentinel.ledger.enums.VerificationStatus;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.CredentialRepository;
import com.sentinel.ledger.repository.StudentRepository;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final StudentRepository studentRepository;
    private final CryptoService cryptoService;
    private final InstitutionSecurityEvaluator securityEvaluator;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public CredentialService(CredentialRepository credentialRepository,
                             StudentRepository studentRepository,
                             CryptoService cryptoService,
                             InstitutionSecurityEvaluator securityEvaluator,
                             AuditService auditService,
                             ObjectMapper objectMapper) {
        this.credentialRepository = credentialRepository;
        this.studentRepository = studentRepository;
        this.cryptoService = cryptoService;
        this.securityEvaluator = securityEvaluator;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CredentialDto issueCredential(IssueCredentialRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        securityEvaluator.validateInstitutionAccess(student.getInstitution().getId());

        // Eligibility Check 1: Verification Status must be VERIFIED
        if (student.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new BadRequestException("Student verification status is " + student.getVerificationStatus()
                    + ". Only VERIFIED students are eligible for digital credential issuance.");
        }

        // Eligibility Check 2: Admission Status must be ADMITTED
        if (student.getAdmissionStatus() != AdmissionStatus.ADMITTED && student.getAdmissionStatus() != AdmissionStatus.PROVISIONAL) {
            throw new BadRequestException("Student is in " + student.getAdmissionStatus()
                    + " status and is ineligible for credential issuance.");
        }

        // Eligibility Check 3: Check if active credential already issued
        if (credentialRepository.existsByStudentIdAndStatus(student.getId(), CredentialStatus.VALID)) {
            throw new BadRequestException("An active valid credential has already been issued for student "
                    + student.getStudentName() + " (" + student.getSyntheticApaarId() + "). Reissuance is not permitted while an active credential exists.");
        }

        // Eligibility Check 4: Required academic information completeness
        if (request.getQualification() == null || request.getQualification().trim().isEmpty()) {
            throw new BadRequestException("Qualification is required for credential issuance.");
        }
        if (request.getCompletionYear() == null || request.getCompletionYear() < 1950) {
            throw new BadRequestException("A valid completion/graduation year is required.");
        }

        String credentialId = "AICTE-CRED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate issueDate = request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now();

        // Canonical minimal claims to sign
        Map<String, Object> claims = new TreeMap<>();
        claims.put("credentialId", credentialId);
        claims.put("syntheticSubjectRef", student.getSyntheticApaarId());
        claims.put("institutionCode", student.getInstitution().getCode());
        claims.put("qualification", request.getQualification().trim());
        claims.put("completionYear", request.getCompletionYear());
        claims.put("issueDate", issueDate.toString());

        String canonicalData;
        try {
            canonicalData = objectMapper.writeValueAsString(claims);
        } catch (Exception e) {
            throw new BadRequestException("Failed to canonicalize credential claims");
        }

        String signature = cryptoService.sign(canonicalData);
        String publicKeyPem = cryptoService.getPublicKeyPem();

        Credential cred = new Credential(
                credentialId,
                student,
                student.getSyntheticApaarId(),
                student.getInstitution(),
                request.getQualification().trim(),
                request.getCompletionYear(),
                request.getCgpaOrGrade(),
                student.getInstitution().getName(),
                issueDate,
                CredentialStatus.VALID,
                cryptoService.getKeyId(),
                cryptoService.getAlgorithm(),
                signature,
                publicKeyPem
        );

        Credential saved = credentialRepository.save(cred);
        auditService.log("CREDENTIAL_ISSUED", "CREDENTIAL", saved.getCredentialId(), AuditOutcome.SUCCESS,
                "Issued digital credential for synthetic subject: " + student.getSyntheticApaarId());

        return toDto(saved);
    }

    public VerificationResultDto verifyCredential(VerifyCredentialRequest request) {
        VerificationResultDto result = new VerificationResultDto();
        result.setCredentialId(request.getCredentialId());

        Credential cred = credentialRepository.findByCredentialId(request.getCredentialId()).orElse(null);

        if (cred == null) {
            result.setValid(false);
            result.setSignatureValid(false);
            result.setCredentialStatus("NOT_FOUND");
            result.setVerificationNotes("No matching issued credential record found in AICTE registry.");
            auditService.log("CREDENTIAL_VERIFIED", "CREDENTIAL", request.getCredentialId(), AuditOutcome.FAILURE, "Credential not found");
            return result;
        }

        // Canonical claims check
        Map<String, Object> claims = new TreeMap<>();
        claims.put("credentialId", cred.getCredentialId());
        claims.put("syntheticSubjectRef", cred.getSyntheticSubjectRef());
        claims.put("institutionCode", cred.getInstitution().getCode());
        claims.put("qualification", cred.getQualification());
        claims.put("completionYear", cred.getCompletionYear());
        claims.put("issueDate", cred.getIssueDate().toString());

        String canonicalData;
        try {
            canonicalData = objectMapper.writeValueAsString(claims);
        } catch (Exception e) {
            canonicalData = "";
        }

        // If client provided custom payload or signature (tamper simulation)
        String signatureToVerify = request.getSignature() != null ? request.getSignature() : cred.getSignature();
        String dataToVerify = request.getPayloadJson() != null ? request.getPayloadJson() : canonicalData;
        String pubKey = request.getPublicKeyPem() != null ? request.getPublicKeyPem() : cred.getPublicKeyPem();

        boolean sigValid = cryptoService.verify(dataToVerify, signatureToVerify, pubKey);

        result.setSignatureValid(sigValid);
        result.setAlgorithm(cred.getAlgorithm());
        result.setKeyId(cred.getKeyId());
        result.setIssuer(cred.getIssuer());
        result.setInstitutionName(cred.getInstitution().getName());
        result.setQualification(cred.getQualification());
        result.setCompletionYear(cred.getCompletionYear());
        result.setIssueDate(cred.getIssueDate());
        result.setSyntheticSubjectRef(cred.getSyntheticSubjectRef());

        // Resolve authenticated user context if any
        UserPrincipal currentUser = null;
        try {
            currentUser = securityEvaluator.getCurrentUser();
        } catch (Exception ignored) {
            // Unauthenticated / public verification request
        }

        // Check Registrar Institutional Verification Restriction (Section 7.2)
        if (currentUser != null && currentUser.getRole() == Role.COLLEGE_REGISTRAR) {
            Long userInstId = currentUser.getInstitutionId();
            if (userInstId != null && !userInstId.equals(cred.getInstitution().getId())) {
                result.setValid(false);
                result.setCredentialStatus("INSTITUTION_MISMATCH");
                result.setVerificationNotes("Access Restricted: As a College Registrar, you are authorized to verify credentials only for students belonging to your own institution.");
                result.setMaskedStudentName("N/A (Cross-Institution)");
                result.setStudentName(null);
                auditService.log("CREDENTIAL_VERIFY_CROSS_INSTITUTION_BLOCKED", "CREDENTIAL", cred.getCredentialId(),
                        AuditOutcome.FAILURE, "Registrar from institution ID " + userInstId + " attempted verification of credential belonging to institution ID " + cred.getInstitution().getId());
                return result;
            }
        }

        // Student name disclosure: Full name disclosed ONLY to authorized institutional registrar (Section 7.3)
        if (currentUser != null && currentUser.getRole() == Role.COLLEGE_REGISTRAR &&
                currentUser.getInstitutionId() != null && currentUser.getInstitutionId().equals(cred.getInstitution().getId())) {
            result.setStudentName(cred.getStudent() != null ? cred.getStudent().getStudentName() : null);
            result.setMaskedStudentName(cred.getStudent() != null ? cred.getStudent().getStudentName() : null);
        } else {
            // Masked minimal disclosure for public / verifiers
            if (cred.getStudent() != null) {
                result.setMaskedStudentName(maskName(cred.getStudent().getStudentName()));
            } else {
                result.setMaskedStudentName("S**** R****");
            }
            result.setStudentName(null);
        }

        // Check Signature Validity
        if (!sigValid) {
            result.setValid(false);
            result.setCredentialStatus("TAMPERED");
            result.setVerificationNotes("Cryptographic signature verification FAILED! Payload or signature has been modified or corrupted.");
            auditService.log("CREDENTIAL_VERIFY_TAMPER_DETECTED", "CREDENTIAL", cred.getCredentialId(), AuditOutcome.FAILURE, "Cryptographic signature check failed");
            return result;
        }

        // Check Graduation Year Expiry (Section 7.1: valid through Dec 31 of completionYear)
        int currentYear = LocalDate.now().getYear();
        if (cred.getCompletionYear() != null && currentYear > cred.getCompletionYear()) {
            result.setValid(false);
            result.setCredentialStatus("EXPIRED");
            result.setVerificationNotes("Credential validity expired on December 31, " + cred.getCompletionYear()
                    + " pursuant to AICTE Graduation Year validity policy.");
            auditService.log("CREDENTIAL_VERIFY_EXPIRED", "CREDENTIAL", cred.getCredentialId(), AuditOutcome.FAILURE,
                    "Credential completion year " + cred.getCompletionYear() + " has elapsed.");
            return result;
        }

        // Check Revocation/Status
        if (cred.getStatus() != CredentialStatus.VALID) {
            result.setValid(false);
            result.setCredentialStatus(cred.getStatus().name());
            result.setVerificationNotes("Signature is mathematically valid, but the credential status is " + cred.getStatus().name() + ".");
            auditService.log("CREDENTIAL_VERIFIED_INACTIVE", "CREDENTIAL", cred.getCredentialId(), AuditOutcome.FAILURE, "Status is " + cred.getStatus());
            return result;
        }

        result.setValid(true);
        result.setCredentialStatus("VALID");
        result.setVerificationNotes("Valid digital signature and authentic active credential verified against AICTE EduTrust trust registry.");
        auditService.log("CREDENTIAL_VERIFIED_SUCCESS", "CREDENTIAL", cred.getCredentialId(), AuditOutcome.SUCCESS, "Verified successfully");

        return result;
    }

    public Page<CredentialDto> getCredentials(Long institutionId, Pageable pageable) {
        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);
        if (effectiveInstId != null) {
            return credentialRepository.findByInstitutionId(effectiveInstId, pageable).map(this::toDto);
        }
        return credentialRepository.findAll(pageable).map(this::toDto);
    }

    private String maskName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "N/A";
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() <= 1) {
                sb.append(part).append(" ");
            } else {
                sb.append(part.charAt(0))
                        .append("*".repeat(Math.min(part.length() - 1, 4)))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    public CredentialDto toDto(Credential c) {
        CredentialDto dto = new CredentialDto();
        dto.setId(c.getId());
        dto.setCredentialId(c.getCredentialId());
        if (c.getStudent() != null) {
            dto.setStudentId(c.getStudent().getId());
            dto.setStudentName(c.getStudent().getStudentName());
        }
        dto.setSyntheticSubjectRef(c.getSyntheticSubjectRef());
        dto.setInstitutionId(c.getInstitution().getId());
        dto.setInstitutionName(c.getInstitution().getName());
        dto.setQualification(c.getQualification());
        dto.setCompletionYear(c.getCompletionYear());
        dto.setCgpaOrGrade(c.getCgpaOrGrade());
        dto.setIssuer(c.getIssuer());
        dto.setIssueDate(c.getIssueDate());
        dto.setStatus(c.getStatus());
        dto.setKeyId(c.getKeyId());
        dto.setAlgorithm(c.getAlgorithm());
        dto.setSignature(c.getSignature());
        dto.setPublicKeyPem(c.getPublicKeyPem());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
