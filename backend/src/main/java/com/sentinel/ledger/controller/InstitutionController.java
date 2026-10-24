package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.CreateInstitutionRequest;
import com.sentinel.ledger.dto.InstitutionDto;
import com.sentinel.ledger.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
@Tag(name = "Institution Management", description = "AICTE-approved institution registry and intake configurations")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    @Operation(summary = "List all AICTE-approved institutions with summary stats")
    public ResponseEntity<List<InstitutionDto>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get institution details by ID")
    public ResponseEntity<InstitutionDto> getInstitutionById(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.getInstitutionById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('AICTE_ADMIN')")
    @Operation(summary = "Register a new institution (AICTE Admin only)")
    public ResponseEntity<InstitutionDto> createInstitution(@Valid @RequestBody CreateInstitutionRequest request) {
        return new ResponseEntity<>(institutionService.createInstitution(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AICTE_ADMIN')")
    @Operation(summary = "Update institution settings and configured intake (AICTE Admin only)")
    public ResponseEntity<InstitutionDto> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody CreateInstitutionRequest request) {
        return ResponseEntity.ok(institutionService.updateInstitution(id, request));
    }
}
