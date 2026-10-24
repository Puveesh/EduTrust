package com.sentinel.ledger.service;

import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.CreateInstitutionRequest;
import com.sentinel.ledger.dto.InstitutionDto;
import com.sentinel.ledger.entity.Institution;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.InstitutionRepository;
import com.sentinel.ledger.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    public InstitutionService(InstitutionRepository institutionRepository,
                              StudentRepository studentRepository,
                              AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    public List<InstitutionDto> getAllInstitutions() {
        return institutionRepository.findAll().stream().map(inst -> {
            InstitutionDto dto = toDto(inst);
            dto.setTotalStudents(studentRepository.countByInstitutionId(inst.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    public InstitutionDto getInstitutionById(Long id) {
        Institution inst = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + id));
        InstitutionDto dto = toDto(inst);
        dto.setTotalStudents(studentRepository.countByInstitutionId(inst.getId()));
        return dto;
    }

    @Transactional
    public InstitutionDto createInstitution(CreateInstitutionRequest request) {
        if (institutionRepository.existsByCode(request.getCode().trim())) {
            throw new BadRequestException("Institution code already exists: " + request.getCode());
        }

        Institution inst = new Institution(
                request.getName().trim(),
                request.getCode().trim().toUpperCase(),
                request.getState().trim(),
                request.getCity().trim(),
                request.getApprovalStatus() != null ? request.getApprovalStatus() : "APPROVED",
                request.getConfiguredIntake() != null ? request.getConfiguredIntake() : 120,
                request.getContactEmail(),
                request.getContactPhone()
        );

        Institution saved = institutionRepository.save(inst);
        auditService.log("INSTITUTION_CREATED", "INSTITUTION", saved.getId().toString(), AuditOutcome.SUCCESS,
                "New institution registered: " + saved.getName() + " (" + saved.getCode() + ")");

        return toDto(saved);
    }

    @Transactional
    public InstitutionDto updateInstitution(Long id, CreateInstitutionRequest request) {
        Institution inst = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + id));

        inst.setName(request.getName().trim());
        inst.setState(request.getState().trim());
        inst.setCity(request.getCity().trim());
        if (request.getApprovalStatus() != null) inst.setApprovalStatus(request.getApprovalStatus());
        if (request.getConfiguredIntake() != null) inst.setConfiguredIntake(request.getConfiguredIntake());
        inst.setContactEmail(request.getContactEmail());
        inst.setContactPhone(request.getContactPhone());

        Institution updated = institutionRepository.save(inst);
        auditService.log("INSTITUTION_UPDATED", "INSTITUTION", updated.getId().toString(), AuditOutcome.SUCCESS,
                "Institution details updated: " + updated.getName());

        return toDto(updated);
    }

    public InstitutionDto toDto(Institution inst) {
        InstitutionDto dto = new InstitutionDto();
        dto.setId(inst.getId());
        dto.setName(inst.getName());
        dto.setCode(inst.getCode());
        dto.setState(inst.getState());
        dto.setCity(inst.getCity());
        dto.setApprovalStatus(inst.getApprovalStatus());
        dto.setConfiguredIntake(inst.getConfiguredIntake());
        dto.setContactEmail(inst.getContactEmail());
        dto.setContactPhone(inst.getContactPhone());
        dto.setCreatedAt(inst.getCreatedAt());
        dto.setUpdatedAt(inst.getUpdatedAt());
        return dto;
    }
}
