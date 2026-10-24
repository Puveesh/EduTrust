package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.ResolveCaseRequest;
import com.sentinel.ledger.dto.ReviewCaseDto;
import com.sentinel.ledger.enums.CaseStatus;
import com.sentinel.ledger.enums.CaseType;
import com.sentinel.ledger.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Center", description = "Review and resolution center for duplicate candidates, intake violations, and invalid data")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "List flagged review cases with filters")
    public ResponseEntity<Page<ReviewCaseDto>> getCases(
            @RequestParam(required = false) CaseType caseType,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortParams[0]);

        return ResponseEntity.ok(reviewService.getCases(caseType, status, institutionId, PageRequest.of(page, size, sortObj)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review case details and associated evidence")
    public ResponseEntity<ReviewCaseDto> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getCaseById(id));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve or update status of a review case with mandatory reason")
    public ResponseEntity<ReviewCaseDto> resolveCase(
            @PathVariable Long id,
            @Valid @RequestBody ResolveCaseRequest request) {
        return ResponseEntity.ok(reviewService.resolveCase(id, request));
    }

    @PostMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss or mark review case as false positive with mandatory reason")
    public ResponseEntity<ReviewCaseDto> dismissCase(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) Map<String, String> body) {
        String effectiveReason = reason;
        if ((effectiveReason == null || effectiveReason.trim().isEmpty()) && body != null && body.containsKey("reason")) {
            effectiveReason = body.get("reason");
        }
        return ResponseEntity.ok(reviewService.dismissCase(id, effectiveReason));
    }
}
