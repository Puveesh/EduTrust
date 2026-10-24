package com.sentinel.ledger.service;

import com.sentinel.ledger.dto.IssueCredentialRequest;
import com.sentinel.ledger.entity.*;
import com.sentinel.ledger.enums.*;
import com.sentinel.ledger.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataSeedService implements CommandLineRunner {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final ProgramIntakeRepository programIntakeRepository;
    private final StudentRepository studentRepository;
    private final ReviewCaseRepository reviewCaseRepository;
    private final AuditLogRepository auditLogRepository;
    private final CredentialService credentialService;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;

    public DataSeedService(InstitutionRepository institutionRepository,
                           UserRepository userRepository,
                           ProgramIntakeRepository programIntakeRepository,
                           StudentRepository studentRepository,
                           ReviewCaseRepository reviewCaseRepository,
                           AuditLogRepository auditLogRepository,
                           CredentialService credentialService,
                           PasswordEncoder passwordEncoder,
                           CryptoService cryptoService) {
        this.institutionRepository = institutionRepository;
        this.userRepository = userRepository;
        this.programIntakeRepository = programIntakeRepository;
        this.studentRepository = studentRepository;
        this.reviewCaseRepository = reviewCaseRepository;
        this.auditLogRepository = auditLogRepository;
        this.credentialService = credentialService;
        this.passwordEncoder = passwordEncoder;
        this.cryptoService = cryptoService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (institutionRepository.count() > 0) {
            return; // Already seeded
        }

        // 1. Seed 3 Synthetic Institutions
        Institution instDelhi = institutionRepository.save(new Institution(
                "Delhi Institute of Advanced Technology",
                "AICTE-DEL-101",
                "Delhi",
                "New Delhi",
                "APPROVED",
                180,
                "contact@diat.ac.in",
                "+91-11-23456789"
        ));

        Institution instMaha = institutionRepository.save(new Institution(
                "Maharashtra Institute of Technology & Research",
                "AICTE-MAH-202",
                "Maharashtra",
                "Pune",
                "APPROVED",
                240,
                "admissions@mitr.edu.in",
                "+91-20-87654321"
        ));

        Institution instKar = institutionRepository.save(new Institution(
                "Karnataka Engineering Academy",
                "AICTE-KAR-303",
                "Karnataka",
                "Bengaluru",
                "APPROVED",
                150,
                "registrar@kea.ac.in",
                "+91-80-33445566"
        ));

        // 2. Seed Users
        User adminUser = userRepository.save(new User(
                "admin",
                passwordEncoder.encode("Admin@123"),
                "AICTE Central Administrator",
                "admin@aicte-india.org",
                Role.AICTE_ADMIN,
                null
        ));

        // Establish admin security context for seed operations
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        new com.sentinel.ledger.security.UserPrincipal(adminUser),
                        null,
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_AICTE_ADMIN"))
                )
        );

        userRepository.save(new User(
                "registrar_delhi",
                passwordEncoder.encode("Registrar@123"),
                "Dr. Rajesh Kumar",
                "registrar@diat.ac.in",
                Role.COLLEGE_REGISTRAR,
                instDelhi
        ));

        userRepository.save(new User(
                "registrar_maha",
                passwordEncoder.encode("Registrar@123"),
                "Prof. Sunita Deshmukh",
                "registrar@mitr.edu.in",
                Role.COLLEGE_REGISTRAR,
                instMaha
        ));

        userRepository.save(new User(
                "verifier_corp",
                passwordEncoder.encode("Verifier@123"),
                "TCS Verification Wing",
                "verifier@tcs.com",
                Role.VERIFIER,
                null
        ));

        // 3. Seed Program Intakes
        String[] programs = {
                "Computer Science and Engineering",
                "Information Technology",
                "Electronics and Communication",
                "Mechanical Engineering"
        };

        for (Institution inst : List.of(instDelhi, instMaha, instKar)) {
            for (String prog : programs) {
                programIntakeRepository.save(new ProgramIntake(
                        inst,
                        prog,
                        "2024-2025",
                        60,
                        "{\"GENERAL\": 30, \"OBC\": 16, \"SC\": 9, \"ST\": 5}",
                        "AICTE_REG_2024_V1"
                ));
            }
        }

        // 4. Seed Synthetic Students
        String[] firstNames = {"Aarav", "Priya", "Rohan", "Ananya", "Vikram", "Neha", "Aditya", "Kavya", "Rahul", "Pooja", "Siddharth", "Isha", "Manish", "Divya", "Karan"};
        String[] lastNames = {"Sharma", "Patel", "Verma", "Iyer", "Deshmukh", "Reddy", "Mehta", "Singh", "Nair", "Joshi", "Chopra", "Gupta", "Kulkarni", "Bose", "Rao"};
        AdmissionCategory[] categories = AdmissionCategory.values();
        Random rnd = new Random(42);

        List<Student> seededStudents = new ArrayList<>();
        int studentCounter = 1000;

        for (Institution inst : List.of(instDelhi, instMaha, instKar)) {
            int countForInst = (inst == instDelhi) ? 30 : (inst == instMaha ? 35 : 20);
            for (int i = 0; i < countForInst; i++) {
                studentCounter++;
                String fName = firstNames[rnd.nextInt(firstNames.length)];
                String lName = lastNames[rnd.nextInt(lastNames.length)];
                String prog = programs[rnd.nextInt(programs.length)];
                AdmissionCategory cat = categories[rnd.nextInt(categories.length)];
                String apaarId = "APAAR-SYN-2024-" + studentCounter;
                String enrollNo = "ENR-" + inst.getCode() + "-" + studentCounter;

                Student s = new Student(
                        inst,
                        apaarId,
                        enrollNo,
                        fName + " " + lName,
                        prog,
                        "2024-2025",
                        2024,
                        cat,
                        AdmissionStatus.ADMITTED,
                        VerificationStatus.VERIFIED,
                        rnd.nextBoolean() ? "MALE" : "FEMALE",
                        LocalDate.of(2002 + rnd.nextInt(4), 1 + rnd.nextInt(12), 1 + rnd.nextInt(28))
                );
                seededStudents.add(studentRepository.save(s));
            }
        }

        // 5. Seed duplicate candidate across Delhi and Maharashtra
        String sharedSyntheticId = "APAAR-SYN-2024-9999";
        Student dupStudent1 = studentRepository.save(new Student(
                instDelhi,
                sharedSyntheticId,
                "ENR-AICTE-DEL-9999",
                "Arjun Singhania",
                "Computer Science and Engineering",
                "2024-2025",
                2024,
                AdmissionCategory.GENERAL,
                AdmissionStatus.ADMITTED,
                VerificationStatus.PENDING_REVIEW,
                "MALE",
                LocalDate.of(2003, 5, 14)
        ));

        Student dupStudent2 = studentRepository.save(new Student(
                instMaha,
                sharedSyntheticId,
                "ENR-AICTE-MAH-9999",
                "Arjun Singhania",
                "Information Technology",
                "2024-2025",
                2024,
                AdmissionCategory.GENERAL,
                AdmissionStatus.PROVISIONAL,
                VerificationStatus.PENDING_REVIEW,
                "MALE",
                LocalDate.of(2003, 5, 14)
        ));

        // Create Review Case for Duplicate Candidate
        reviewCaseRepository.save(new ReviewCase(
                "REV-DUP-2024-001",
                CaseType.DUPLICATE_CANDIDATE,
                CaseStatus.OPEN,
                Severity.HIGH,
                instDelhi,
                dupStudent1,
                String.format("{\"matchedStudentId\": %d, \"matchedStudentName\": \"%s\", \"matchedInstitutionId\": %d, \"matchedInstitutionName\": \"%s\", \"syntheticApaarId\": \"%s\", \"note\": \"Possible cross-institution duplicate candidate detected.\"}",
                        dupStudent2.getId(), dupStudent2.getStudentName(), instMaha.getId(), instMaha.getName(), sharedSyntheticId)
        ));

        // Create Review Case for Intake Violation
        reviewCaseRepository.save(new ReviewCase(
                "REV-INT-2024-002",
                CaseType.INTAKE_VIOLATION,
                CaseStatus.OPEN,
                Severity.MEDIUM,
                instDelhi,
                dupStudent1,
                "{\"program\": \"Mechanical Engineering\", \"approvedIntake\": 60, \"admittedCount\": 63, \"excessIntake\": 3, \"policyVersion\": \"AICTE_REG_2024_V1\"}"
        ));

        // Create Review Case for Invalid Data
        reviewCaseRepository.save(new ReviewCase(
                "REV-DAT-2024-003",
                CaseType.INVALID_DATA,
                CaseStatus.UNDER_REVIEW,
                Severity.LOW,
                instMaha,
                dupStudent2,
                "{\"field\": \"admissionCategory\", \"submittedValue\": \"MANAGEMENT_QUOTA_SPECIAL\", \"issue\": \"Non-standard AICTE category format\"}"
        ));

        // 6. Seed a sample digitally signed credential
        Student sampleStudent = seededStudents.get(0);
        IssueCredentialRequest credReq = new IssueCredentialRequest();
        credReq.setStudentId(sampleStudent.getId());
        credReq.setQualification("Bachelor of Technology in Computer Science");
        credReq.setCompletionYear(2024);
        credReq.setCgpaOrGrade("9.2 CGPA");
        credReq.setIssueDate(LocalDate.now());

        credentialService.issueCredential(credReq);

        // 7. Seed Initial Audit Logs
        auditLogRepository.save(new AuditLog(1L, "admin", null, "SYSTEM_BOOTSTRAP", "SYSTEM", "BOOTSTRAP_01", AuditOutcome.SUCCESS, "Platform database initialized and seeded with synthetic baseline data", "127.0.0.1", "INIT-001"));
        auditLogRepository.save(new AuditLog(2L, "registrar_delhi", instDelhi.getId(), "STUDENT_BATCH_IMPORT", "STUDENT", "BATCH_01", AuditOutcome.SUCCESS, "Imported 30 synthetic student records for AY 2024-2025", "127.0.0.1", "INIT-002"));
        auditLogRepository.save(new AuditLog(3L, "registrar_maha", instMaha.getId(), "STUDENT_BATCH_IMPORT", "STUDENT", "BATCH_02", AuditOutcome.SUCCESS, "Imported 35 synthetic student records for AY 2024-2025", "127.0.0.1", "INIT-003"));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}
