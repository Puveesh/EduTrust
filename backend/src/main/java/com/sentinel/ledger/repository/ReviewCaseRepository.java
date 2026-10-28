package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.ReviewCase;
import com.sentinel.ledger.enums.CaseStatus;
import com.sentinel.ledger.enums.CaseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewCaseRepository extends JpaRepository<ReviewCase, Long>, JpaSpecificationExecutor<ReviewCase> {
    Optional<ReviewCase> findByCaseNumber(String caseNumber);
    long countByStatus(CaseStatus status);
    long countByCaseType(CaseType caseType);
    long countByInstitutionIdAndStatus(Long institutionId, CaseStatus status);
    long countByInstitutionIdAndCaseType(Long institutionId, CaseType caseType);
    long countByInstitutionIdAndCaseTypeAndStatus(Long institutionId, CaseType caseType, CaseStatus status);

    @Query("SELECT COUNT(c) FROM ReviewCase c WHERE c.caseType = 'DUPLICATE_CANDIDATE' AND c.status = 'OPEN'")
    long countOpenDuplicateCases();

    Page<ReviewCase> findByInstitutionId(Long institutionId, Pageable pageable);
    Page<ReviewCase> findByStatus(CaseStatus status, Pageable pageable);
}
