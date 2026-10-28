package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.ProgramIntakeDto;
import com.sentinel.ledger.service.IntakeComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
@Tag(name = "Intake Compliance", description = "Monitor approved intake limits vs admitted enrollment numbers")
public class ComplianceController {

    private final IntakeComplianceService complianceService;

    public ComplianceController(IntakeComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/intake")
    @Operation(summary = "Get intake utilization and compliance status per institution and program")
    public ResponseEntity<List<ProgramIntakeDto>> getIntakeCompliance(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(complianceService.getIntakeCompliance(institutionId, academicYear));
    }
}
