package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.CredentialDto;
import com.sentinel.ledger.dto.IssueCredentialRequest;
import com.sentinel.ledger.dto.VerificationResultDto;
import com.sentinel.ledger.dto.VerifyCredentialRequest;
import com.sentinel.ledger.service.CredentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credentials")
@Tag(name = "Credential Verification", description = "Digitally signed credential issuance and minimal disclosure verification")
public class CredentialController {

    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("/issue")
    @Operation(summary = "Issue a digitally signed academic credential")
    public ResponseEntity<CredentialDto> issueCredential(@Valid @RequestBody IssueCredentialRequest request) {
        return new ResponseEntity<>(credentialService.issueCredential(request), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify an issued credential signature and active status with minimal disclosure")
    public ResponseEntity<VerificationResultDto> verifyCredential(@Valid @RequestBody VerifyCredentialRequest request) {
        return ResponseEntity.ok(credentialService.verifyCredential(request));
    }

    @GetMapping
    @Operation(summary = "List issued credentials for institution")
    public ResponseEntity<Page<CredentialDto>> getCredentials(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(credentialService.getCredentials(institutionId, PageRequest.of(page, size)));
    }
}
