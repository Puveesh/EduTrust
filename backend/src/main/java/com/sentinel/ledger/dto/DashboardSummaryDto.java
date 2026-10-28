package com.sentinel.ledger.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {
    private long totalStudents;
    private long totalInstitutions;
    private long recordsAwaitingReview;
    private long totalImportBatches;
    private long openDuplicateCases;
    private long intakeViolationsCount;
    private Map<String, Long> studentsByInstitution;
    private Map<String, Long> studentsByAcademicYear;
    private Map<String, Long> studentsByStatus;
    private Map<String, Long> studentsByProgram;
    private List<AuditLogDto> recentAuditLogs;
    private List<ReviewCaseDto> recentReviewCases;
    private List<ProgramIntakeDto> programIntakeOverview;

    public DashboardSummaryDto() {}

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalInstitutions() { return totalInstitutions; }
    public void setTotalInstitutions(long totalInstitutions) { this.totalInstitutions = totalInstitutions; }

    public long getRecordsAwaitingReview() { return recordsAwaitingReview; }
    public void setRecordsAwaitingReview(long recordsAwaitingReview) { this.recordsAwaitingReview = recordsAwaitingReview; }

    public long getTotalImportBatches() { return totalImportBatches; }
    public void setTotalImportBatches(long totalImportBatches) { this.totalImportBatches = totalImportBatches; }

    public long getOpenDuplicateCases() { return openDuplicateCases; }
    public void setOpenDuplicateCases(long openDuplicateCases) { this.openDuplicateCases = openDuplicateCases; }

    public long getIntakeViolationsCount() { return intakeViolationsCount; }
    public void setIntakeViolationsCount(long intakeViolationsCount) { this.intakeViolationsCount = intakeViolationsCount; }

    public Map<String, Long> getStudentsByInstitution() { return studentsByInstitution; }
    public void setStudentsByInstitution(Map<String, Long> studentsByInstitution) { this.studentsByInstitution = studentsByInstitution; }

    public Map<String, Long> getStudentsByAcademicYear() { return studentsByAcademicYear; }
    public void setStudentsByAcademicYear(Map<String, Long> studentsByAcademicYear) { this.studentsByAcademicYear = studentsByAcademicYear; }

    public Map<String, Long> getStudentsByStatus() { return studentsByStatus; }
    public void setStudentsByStatus(Map<String, Long> studentsByStatus) { this.studentsByStatus = studentsByStatus; }

    public Map<String, Long> getStudentsByProgram() { return studentsByProgram; }
    public void setStudentsByProgram(Map<String, Long> studentsByProgram) { this.studentsByProgram = studentsByProgram; }

    public List<AuditLogDto> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLogDto> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }

    public List<ReviewCaseDto> getRecentReviewCases() { return recentReviewCases; }
    public void setRecentReviewCases(List<ReviewCaseDto> recentReviewCases) { this.recentReviewCases = recentReviewCases; }

    public List<ProgramIntakeDto> getProgramIntakeOverview() { return programIntakeOverview; }
    public void setProgramIntakeOverview(List<ProgramIntakeDto> programIntakeOverview) { this.programIntakeOverview = programIntakeOverview; }
}
