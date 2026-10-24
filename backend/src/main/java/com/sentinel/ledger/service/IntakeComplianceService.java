package com.sentinel.ledger.service;

import com.sentinel.ledger.dto.ProgramIntakeDto;
import com.sentinel.ledger.entity.Institution;
import com.sentinel.ledger.entity.ProgramIntake;
import com.sentinel.ledger.repository.InstitutionRepository;
import com.sentinel.ledger.repository.ProgramIntakeRepository;
import com.sentinel.ledger.repository.StudentRepository;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IntakeComplianceService {

    private final ProgramIntakeRepository intakeRepository;
    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;
    private final InstitutionSecurityEvaluator securityEvaluator;

    public IntakeComplianceService(ProgramIntakeRepository intakeRepository,
                                  InstitutionRepository institutionRepository,
                                  StudentRepository studentRepository,
                                  InstitutionSecurityEvaluator securityEvaluator) {
        this.intakeRepository = intakeRepository;
        this.institutionRepository = institutionRepository;
        this.studentRepository = studentRepository;
        this.securityEvaluator = securityEvaluator;
    }

    public List<ProgramIntakeDto> getIntakeCompliance(Long institutionId, String academicYear) {
        Long effectiveInstId = securityEvaluator.resolveTargetInstitutionId(institutionId);

        List<Institution> institutions;
        if (effectiveInstId != null) {
            institutions = institutionRepository.findById(effectiveInstId).map(List::of).orElse(List.of());
        } else {
            institutions = institutionRepository.findAll();
        }

        List<ProgramIntakeDto> results = new ArrayList<>();
        String targetYear = (academicYear != null && !academicYear.trim().isEmpty()) ? academicYear : "2024-2025";

        for (Institution inst : institutions) {
            List<ProgramIntake> intakes = intakeRepository.findByInstitutionIdAndAcademicYear(inst.getId(), targetYear);
            if (intakes.isEmpty()) {
                // Synthesize from configured overall institution intake if no program-level split yet
                long totalAdmitted = studentRepository.countByInstitutionId(inst.getId());
                ProgramIntakeDto dto = new ProgramIntakeDto();
                dto.setInstitutionId(inst.getId());
                dto.setInstitutionName(inst.getName());
                dto.setProgramName("Overall Institutional Intake");
                dto.setAcademicYear(targetYear);
                dto.setApprovedIntake(inst.getConfiguredIntake());
                dto.setCurrentAdmitted(totalAdmitted);
                double util = inst.getConfiguredIntake() > 0 ? (totalAdmitted * 100.0 / inst.getConfiguredIntake()) : 0.0;
                dto.setUtilizationPercentage(Math.round(util * 10.0) / 10.0);
                dto.setExceeded(totalAdmitted > inst.getConfiguredIntake());
                dto.setPolicyVersion("AICTE_REG_2024_V1");
                results.add(dto);
            } else {
                for (ProgramIntake pi : intakes) {
                    long admitted = studentRepository.countByInstitutionIdAndProgramAndAcademicYear(inst.getId(), pi.getProgramName(), targetYear);
                    ProgramIntakeDto dto = new ProgramIntakeDto();
                    dto.setId(pi.getId());
                    dto.setInstitutionId(inst.getId());
                    dto.setInstitutionName(inst.getName());
                    dto.setProgramName(pi.getProgramName());
                    dto.setAcademicYear(pi.getAcademicYear());
                    dto.setApprovedIntake(pi.getApprovedIntake());
                    dto.setCurrentAdmitted(admitted);
                    double util = pi.getApprovedIntake() > 0 ? (admitted * 100.0 / pi.getApprovedIntake()) : 0.0;
                    dto.setUtilizationPercentage(Math.round(util * 10.0) / 10.0);
                    dto.setExceeded(admitted > pi.getApprovedIntake());
                    dto.setPolicyVersion(pi.getPolicyVersion());
                    results.add(dto);
                }
            }
        }

        return results;
    }
}
