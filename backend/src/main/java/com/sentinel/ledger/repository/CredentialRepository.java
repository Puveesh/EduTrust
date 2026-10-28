package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.Credential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByCredentialId(String credentialId);
    Optional<Credential> findBySyntheticSubjectRef(String syntheticSubjectRef);
    Page<Credential> findByInstitutionId(Long institutionId, Pageable pageable);
    boolean existsByStudentIdAndStatus(Long studentId, com.sentinel.ledger.enums.CredentialStatus status);
    Optional<Credential> findTopByStudentIdOrderByCreatedAtDesc(Long studentId);
    java.util.List<Credential> findByStudentId(Long studentId);
}
