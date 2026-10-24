package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByCode(String code);
    boolean existsByCode(String code);
}
