package com.sentinel.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class IssueCredentialRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotNull(message = "Completion year is required")
    private Integer completionYear;

    private String cgpaOrGrade;

    private LocalDate issueDate;

    public IssueCredentialRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getCompletionYear() { return completionYear; }
    public void setCompletionYear(Integer completionYear) { this.completionYear = completionYear; }

    public String getCgpaOrGrade() { return cgpaOrGrade; }
    public void setCgpaOrGrade(String cgpaOrGrade) { this.cgpaOrGrade = cgpaOrGrade; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
}
