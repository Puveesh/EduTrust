package com.sentinel.ledger.service;

import com.sentinel.ledger.audit.AuditService;
import com.sentinel.ledger.dto.UpdateProfileRequest;
import com.sentinel.ledger.dto.UserDto;
import com.sentinel.ledger.entity.User;
import com.sentinel.ledger.enums.AuditOutcome;
import com.sentinel.ledger.exception.ResourceNotFoundException;
import com.sentinel.ledger.repository.UserRepository;
import com.sentinel.ledger.security.InstitutionSecurityEvaluator;
import com.sentinel.ledger.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InstitutionSecurityEvaluator securityEvaluator;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       InstitutionSecurityEvaluator securityEvaluator,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.securityEvaluator = securityEvaluator;
        this.auditService = auditService;
    }

    public UserDto getCurrentUserProfile() {
        UserPrincipal principal = securityEvaluator.getCurrentUser();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));
        return toDto(user);
    }

    @Transactional
    public UserDto updateProfile(UpdateProfileRequest request) {
        UserPrincipal principal = securityEvaluator.getCurrentUser();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        String oldName = user.getFullName();
        user.setFullName(request.getFullName().trim());
        User updated = userRepository.save(user);

        auditService.log("USER_PROFILE_UPDATED", "USER", updated.getId().toString(), AuditOutcome.SUCCESS,
                "User " + updated.getUsername() + " updated full name from '" + oldName + "' to '" + updated.getFullName() + "'");

        return toDto(updated);
    }

    public UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        if (u.getInstitution() != null) {
            dto.setInstitutionId(u.getInstitution().getId());
            dto.setInstitutionName(u.getInstitution().getName());
        }
        dto.setEnabled(u.isEnabled());
        return dto;
    }
}
