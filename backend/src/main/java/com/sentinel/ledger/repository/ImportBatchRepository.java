package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.ImportBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    Page<ImportBatch> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId, Pageable pageable);
    List<ImportBatch> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
    Page<ImportBatch> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
