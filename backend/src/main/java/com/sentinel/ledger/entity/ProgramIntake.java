package com.sentinel.ledger.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "program_intakes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"institution_id", "program_name", "academic_year"})
})
public class ProgramIntake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "program_name", nullable = false, length = 150)
    private String programName;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "approved_intake", nullable = false)
    private Integer approvedIntake;

    @Column(name = "category_quota_json", columnDefinition = "TEXT")
    private String categoryQuotaJson;

    @Column(name = "policy_version", length = 50)
    private String policyVersion = "AICTE_REG_2024_V1";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ProgramIntake() {}

    public ProgramIntake(Institution institution, String programName, String academicYear, Integer approvedIntake, String categoryQuotaJson, String policyVersion) {
        this.institution = institution;
        this.programName = programName;
        this.academicYear = academicYear;
        this.approvedIntake = approvedIntake;
        this.categoryQuotaJson = categoryQuotaJson;
        this.policyVersion = policyVersion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getApprovedIntake() { return approvedIntake; }
    public void setApprovedIntake(Integer approvedIntake) { this.approvedIntake = approvedIntake; }

    public String getCategoryQuotaJson() { return categoryQuotaJson; }
    public void setCategoryQuotaJson(String categoryQuotaJson) { this.categoryQuotaJson = categoryQuotaJson; }

    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
