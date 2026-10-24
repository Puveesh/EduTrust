package com.sentinel.ledger.dto;

import java.time.LocalDateTime;

public class StudentHistoryDto {
    private Long id;
    private Long studentId;
    private String modifiedBy;
    private String changeType;
    private String oldValueJson;
    private String newValueJson;
    private LocalDateTime timestamp;

    public StudentHistoryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getOldValueJson() { return oldValueJson; }
    public void setOldValueJson(String oldValueJson) { this.oldValueJson = oldValueJson; }

    public String getNewValueJson() { return newValueJson; }
    public void setNewValueJson(String newValueJson) { this.newValueJson = newValueJson; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
