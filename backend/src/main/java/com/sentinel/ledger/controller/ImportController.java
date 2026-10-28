package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.*;
import com.sentinel.ledger.enums.ImportRowStatus;
import com.sentinel.ledger.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
@Tag(name = "Data Import", description = "Safe CSV upload, validation preview, row corrections, and transactional commit")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/template")
    @Operation(summary = "Download sample CSV template for student data upload")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csvContent = importService.generateCsvTemplate();
        byte[] bytes = csvContent.getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and preview student CSV data with validation and duplicate checks")
    public ResponseEntity<ImportPreviewResponse> previewUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "institutionId", required = false) Long institutionId) {
        return ResponseEntity.ok(importService.processCsvUpload(file, institutionId));
    }

    @GetMapping
    @Operation(summary = "List all import batches with status and progress")
    public ResponseEntity<Page<ImportBatchDto>> getBatches(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(importService.getBatches(institutionId, PageRequest.of(page, size)));
    }

    @GetMapping("/{batchId}")
    @Operation(summary = "Get specific import batch metadata")
    public ResponseEntity<ImportBatchDto> getBatchById(@PathVariable Long batchId) {
        return ResponseEntity.ok(importService.getBatchById(batchId));
    }

    @GetMapping("/{batchId}/rows")
    @Operation(summary = "Get staged rows for an import batch with status filter")
    public ResponseEntity<Page<ImportRowDto>> getBatchRows(
            @PathVariable Long batchId,
            @RequestParam(required = false) ImportRowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(importService.getBatchRows(batchId, status, PageRequest.of(page, size)));
    }

    @PatchMapping("/{batchId}/rows/{rowId}")
    @Operation(summary = "Correct or exclude an import row before committing")
    public ResponseEntity<ImportRowDto> updateBatchRow(
            @PathVariable Long batchId,
            @PathVariable Long rowId,
            @RequestBody UpdateImportRowRequest request) {
        return ResponseEntity.ok(importService.updateBatchRow(batchId, rowId, request));
    }

    @PostMapping("/{batchId}/cancel")
    @Operation(summary = "Cancel and discard an uncommitted staged import batch")
    public ResponseEntity<ImportBatchDto> cancelBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(importService.cancelBatch(batchId));
    }

    @PostMapping("/{batchId}/commit")
    @Operation(summary = "Transactionally commit valid rows of an import batch into permanent student records")
    public ResponseEntity<CommitBatchResponse> commitBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(importService.commitBatch(batchId));
    }
}
