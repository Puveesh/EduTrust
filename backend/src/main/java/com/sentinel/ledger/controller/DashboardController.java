package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.DashboardSummaryDto;
import com.sentinel.ledger.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Real-time aggregated metrics and statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated dashboard summary with live database counts and groupings")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(dashboardService.getSummary(institutionId, academicYear));
    }
}
