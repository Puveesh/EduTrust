package com.sentinel.ledger.dto;

public class ProgramIntakeDto {
    private Long id;
    private Long institutionId;
    private String institutionName;
    private String programName;
    private String academicYear;
    private Integer approvedIntake;
    private long currentAdmitted;
    private double utilizationPercentage;
    private boolean isExceeded;
    private String policyVersion;

    public ProgramIntakeDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getApprovedIntake() { return approvedIntake; }
    public void setApprovedIntake(Integer approvedIntake) { this.approvedIntake = approvedIntake; }

    public long getCurrentAdmitted() { return currentAdmitted; }
    public void setCurrentAdmitted(long currentAdmitted) { this.currentAdmitted = currentAdmitted; }

    public double getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

    public boolean isExceeded() { return isExceeded; }
    public void setExceeded(boolean exceeded) { isExceeded = exceeded; }

    public String getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(String policyVersion) { this.policyVersion = policyVersion; }
}
