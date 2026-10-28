package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByInstitutionIdOrderByTimestampDesc(Long institutionId, Pageable pageable);
    List<AuditLog> findTop20ByOrderByTimestampDesc();
    List<AuditLog> findTop20ByInstitutionIdOrderByTimestampDesc(Long institutionId);
}
