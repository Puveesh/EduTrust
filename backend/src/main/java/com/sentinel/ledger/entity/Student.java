package com.sentinel.ledger.entity;

import com.sentinel.ledger.enums.AdmissionCategory;
import com.sentinel.ledger.enums.AdmissionStatus;
import com.sentinel.ledger.enums.VerificationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_apaar", columnList = "synthetic_apaar_id"),
    @Index(name = "idx_student_institution", columnList = "institution_id"),
    @Index(name = "idx_student_program_year", columnList = "program, academic_year")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "synthetic_apaar_id", nullable = false, length = 50)
    private String syntheticApaarId;

    @Column(name = "enrollment_number", nullable = false, length = 100)
    private String enrollmentNumber;

    @Column(name = "student_name", nullable = false, length = 200)
    private String studentName;

    @Column(nullable = false, length = 150)
    private String program;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_category", nullable = false, length = 50)
    private AdmissionCategory admissionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_status", nullable = false, length = 50)
    private AdmissionStatus admissionStatus = AdmissionStatus.ADMITTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

    @Column(length = 20)
    private String gender = "UNSPECIFIED";

    private LocalDate dob;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Student() {}

    public Student(Institution institution, String syntheticApaarId, String enrollmentNumber, String studentName, String program, String academicYear, Integer admissionYear, AdmissionCategory admissionCategory, AdmissionStatus admissionStatus, VerificationStatus verificationStatus, String gender, LocalDate dob) {
        this.institution = institution;
        this.syntheticApaarId = syntheticApaarId;
        this.enrollmentNumber = enrollmentNumber;
        this.studentName = studentName;
        this.program = program;
        this.academicYear = academicYear;
        this.admissionYear = admissionYear;
        this.admissionCategory = admissionCategory;
        this.admissionStatus = admissionStatus;
        this.verificationStatus = verificationStatus;
        this.gender = gender;
        this.dob = dob;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getSyntheticApaarId() { return syntheticApaarId; }
    public void setSyntheticApaarId(String syntheticApaarId) { this.syntheticApaarId = syntheticApaarId; }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public AdmissionCategory getAdmissionCategory() { return admissionCategory; }
    public void setAdmissionCategory(AdmissionCategory admissionCategory) { this.admissionCategory = admissionCategory; }

    public AdmissionStatus getAdmissionStatus() { return admissionStatus; }
    public void setAdmissionStatus(AdmissionStatus admissionStatus) { this.admissionStatus = admissionStatus; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
