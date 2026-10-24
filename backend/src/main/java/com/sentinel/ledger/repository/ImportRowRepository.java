package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.ImportRow;
import com.sentinel.ledger.enums.ImportRowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportRowRepository extends JpaRepository<ImportRow, Long> {
    List<ImportRow> findByBatchIdOrderByRowNumberAsc(Long batchId);
    Page<ImportRow> findByBatchId(Long batchId, Pageable pageable);
    Page<ImportRow> findByBatchIdAndStatus(Long batchId, ImportRowStatus status, Pageable pageable);
    long countByBatchIdAndStatus(Long batchId, ImportRowStatus status);
}
