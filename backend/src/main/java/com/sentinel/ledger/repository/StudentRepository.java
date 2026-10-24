package com.sentinel.ledger.repository;

import com.sentinel.ledger.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    Optional<Student> findBySyntheticApaarId(String syntheticApaarId);

    List<Student> findAllBySyntheticApaarId(String syntheticApaarId);

    Page<Student> findByInstitutionId(Long institutionId, Pageable pageable);

    long countByInstitutionId(Long institutionId);

    long countByInstitutionIdAndProgramAndAcademicYear(Long institutionId, String program, String academicYear);

    @Query("SELECT s FROM Student s WHERE s.syntheticApaarId = :syntheticApaarId AND s.institution.id <> :institutionId")
    List<Student> findDuplicatesInOtherInstitutions(@Param("syntheticApaarId") String syntheticApaarId, @Param("institutionId") Long institutionId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.verificationStatus = 'PENDING_REVIEW'")
    long countPendingVerification();

    @Query("SELECT s.program, COUNT(s) FROM Student s GROUP BY s.program")
    List<Object[]> countByProgramGroup();

    @Query("SELECT s.institution.name, COUNT(s) FROM Student s GROUP BY s.institution.name")
    List<Object[]> countByInstitutionGroup();

    @Query("SELECT s.academicYear, COUNT(s) FROM Student s GROUP BY s.academicYear")
    List<Object[]> countByAcademicYearGroup();

    @Query("SELECT s.verificationStatus, COUNT(s) FROM Student s GROUP BY s.verificationStatus")
    List<Object[]> countByStatusGroup();

    @Query("SELECT s.verificationStatus, COUNT(s) FROM Student s WHERE s.institution.id = :institutionId GROUP BY s.verificationStatus")
    List<Object[]> countByStatusGroupForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT s.program, COUNT(s) FROM Student s WHERE s.institution.id = :institutionId GROUP BY s.program")
    List<Object[]> countByProgramGroupForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT s.academicYear, COUNT(s) FROM Student s WHERE s.institution.id = :institutionId GROUP BY s.academicYear")
    List<Object[]> countByAcademicYearGroupForInstitution(@Param("institutionId") Long institutionId);
}
