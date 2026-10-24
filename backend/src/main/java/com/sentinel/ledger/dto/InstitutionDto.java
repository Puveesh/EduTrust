package com.sentinel.ledger.dto;

import java.time.LocalDateTime;

public class InstitutionDto {
    private Long id;
    private String name;
    private String code;
    private String state;
    private String city;
    private String approvalStatus;
    private Integer configuredIntake;
    private String contactEmail;
    private String contactPhone;
    private long totalStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InstitutionDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public Integer getConfiguredIntake() { return configuredIntake; }
    public void setConfiguredIntake(Integer configuredIntake) { this.configuredIntake = configuredIntake; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
