package com.sentinel.ledger.security;

import com.sentinel.ledger.enums.Role;
import com.sentinel.ledger.exception.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityEvaluator")
public class InstitutionSecurityEvaluator {

    public UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    public boolean canAccessInstitution(Long institutionId) {
        UserPrincipal user = getCurrentUser();
        if (user.getRole() == Role.AICTE_ADMIN) {
            return true;
        }
        return user.getInstitutionId() != null && user.getInstitutionId().equals(institutionId);
    }

    public void validateInstitutionAccess(Long institutionId) {
        if (!canAccessInstitution(institutionId)) {
            throw new AccessDeniedException("Access denied: You are not authorized to view or manage records for institution ID: " + institutionId);
        }
    }

    public Long resolveTargetInstitutionId(Long requestedInstitutionId) {
        UserPrincipal user = getCurrentUser();
        if (user.getRole() == Role.AICTE_ADMIN) {
            return requestedInstitutionId; // null or specific
        }
        if (user.getRole() == Role.COLLEGE_REGISTRAR) {
            if (user.getInstitutionId() == null) {
                throw new AccessDeniedException("Registrar has no assigned institution");
            }
            if (requestedInstitutionId != null && !user.getInstitutionId().equals(requestedInstitutionId)) {
                throw new AccessDeniedException("IDOR blocked: You cannot access data for another institution.");
            }
            return user.getInstitutionId();
        }
        throw new AccessDeniedException("Unauthorized role for institution data access");
    }
}
