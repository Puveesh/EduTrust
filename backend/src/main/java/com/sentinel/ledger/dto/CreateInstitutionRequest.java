package com.sentinel.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateInstitutionRequest {
    @NotBlank(message = "Institution name is required")
    private String name;

    @NotBlank(message = "Institution code is required")
    private String code;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    private String approvalStatus = "APPROVED";

    @NotNull(message = "Configured intake is required")
    private Integer configuredIntake = 120;

    private String contactEmail;
    private String contactPhone;

    public CreateInstitutionRequest() {}

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
}
