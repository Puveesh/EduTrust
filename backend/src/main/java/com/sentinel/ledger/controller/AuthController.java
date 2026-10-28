package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.AuthRequest;
import com.sentinel.ledger.dto.AuthResponse;
import com.sentinel.ledger.dto.InstitutionDto;
import com.sentinel.ledger.dto.RegisterRequest;
import com.sentinel.ledger.service.AuthService;
import com.sentinel.ledger.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication, registration, and session token management")
public class AuthController {

    private final AuthService authService;
    private final InstitutionService institutionService;

    public AuthController(AuthService authService, InstitutionService institutionService) {
        this.authService = authService;
        this.institutionService = institutionService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user under any role (AICTE_ADMIN, COLLEGE_REGISTRAR, VERIFIER)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @GetMapping("/institutions")
    @Operation(summary = "List institutions for registration dropdown")
    public ResponseEntity<List<InstitutionDto>> getRegistrationInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }
}
