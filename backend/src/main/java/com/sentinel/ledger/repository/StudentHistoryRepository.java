package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Long> {
    List<StudentHistory> findByStudentIdOrderByTimestampDesc(Long studentId);
}
