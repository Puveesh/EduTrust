package com.sentinel.ledger.security;

import com.sentinel.ledger.entity.User;
import com.sentinel.ledger.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String email;
    private final Role role;
    private final Long institutionId;
    private final String institutionName;
    private final String institutionCode;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        if (user.getInstitution() != null) {
            this.institutionId = user.getInstitution().getId();
            this.institutionName = user.getInstitution().getName();
            this.institutionCode = user.getInstitution().getCode();
        } else {
            this.institutionId = null;
            this.institutionName = null;
            this.institutionCode = null;
        }
        this.enabled = user.isEnabled();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public Long getInstitutionId() { return institutionId; }
    public String getInstitutionName() { return institutionName; }
    public String getInstitutionCode() { return institutionCode; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
