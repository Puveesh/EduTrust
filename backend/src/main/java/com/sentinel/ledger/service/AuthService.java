package com.sentinel.ledger.service;

import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.AuthRequest;
import com.sentinel.ledger.dto.AuthResponse;
import com.sentinel.ledger.dto.RegisterRequest;
import com.sentinel.ledger.entity.Institution;
import com.sentinel.ledger.entity.User;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.enums.Role;
import com.sentinel.ledger.exception.BadRequestException;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.InstitutionRepository;
import com.sentinel.ledger.repository.UserRepository;
import com.sentinel.ledger.security.JwtTokenProvider;
import com.sentinel.ledger.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                       UserRepository userRepository, InstitutionRepository institutionRepository,
                       PasswordEncoder passwordEncoder, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new BadRequestException("User record not found"));

            auditService.log("USER_LOGIN", "USER", user.getId().toString(), AuditOutcome.SUCCESS, "User logged in successfully");

            Long instId = user.getInstitution() != null ? user.getInstitution().getId() : null;
            String instName = user.getInstitution() != null ? user.getInstitution().getName() : null;
            String instCode = user.getInstitution() != null ? user.getInstitution().getCode() : null;

            return new AuthResponse(token, user.getId(), user.getUsername(), user.getFullName(),
                    user.getEmail(), user.getRole(), instId, instName, instCode);
        } catch (Exception ex) {
            auditService.log("USER_LOGIN_FAILED", "USER", request.getUsername(), AuditOutcome.FAILURE, ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new BadRequestException("Username '" + request.getUsername().trim() + "' is already registered");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new BadRequestException("Email '" + request.getEmail().trim() + "' is already registered");
        }

        Institution institution = null;
        if (request.getRole() == Role.COLLEGE_REGISTRAR) {
            if (request.getInstitutionId() == null) {
                throw new BadRequestException("An institution must be selected for College Registrar accounts");
            }
            institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found with ID: " + request.getInstitutionId()));
        } else if (request.getInstitutionId() != null) {
            institution = institutionRepository.findById(request.getInstitutionId()).orElse(null);
        }

        User user = new User(
                request.getUsername().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                request.getEmail().trim(),
                request.getRole(),
                institution
        );

        User saved = userRepository.save(user);

        auditService.log("USER_REGISTERED", "USER", saved.getId().toString(), AuditOutcome.SUCCESS,
                "Registered new user account: " + saved.getUsername() + " with role " + saved.getRole());

        // Authenticate new user and return AuthResponse
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        Long instId = saved.getInstitution() != null ? saved.getInstitution().getId() : null;
        String instName = saved.getInstitution() != null ? saved.getInstitution().getName() : null;
        String instCode = saved.getInstitution() != null ? saved.getInstitution().getCode() : null;

        return new AuthResponse(token, saved.getId(), saved.getUsername(), saved.getFullName(),
                saved.getEmail(), saved.getRole(), instId, instName, instCode);
    }
}
