package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.ProgramIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramIntakeRepository extends JpaRepository<ProgramIntake, Long> {
    List<ProgramIntake> findByInstitutionId(Long institutionId);
    List<ProgramIntake> findByInstitutionIdAndAcademicYear(Long institutionId, String academicYear);
    Optional<ProgramIntake> findByInstitutionIdAndProgramNameAndAcademicYear(Long institutionId, String programName, String academicYear);
}
