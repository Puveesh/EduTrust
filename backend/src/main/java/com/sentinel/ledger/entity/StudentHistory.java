package com.sentinel.ledger.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_histories", indexes = {
    @Index(name = "idx_history_student", columnList = "student_id")
})
public class StudentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "modified_by", nullable = false, length = 100)
    private String modifiedBy;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "old_value_json", columnDefinition = "TEXT")
    private String oldValueJson;

    @Column(name = "new_value_json", columnDefinition = "TEXT")
    private String newValueJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public StudentHistory() {}

    public StudentHistory(Student student, String modifiedBy, String changeType, String oldValueJson, String newValueJson) {
        this.student = student;
        this.modifiedBy = modifiedBy;
        this.changeType = changeType;
        this.oldValueJson = oldValueJson;
        this.newValueJson = newValueJson;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

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
