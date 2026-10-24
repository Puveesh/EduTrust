package com.sentinel.ledger.controller;

import com.sentinel.ledger.dto.UpdateProfileRequest;
import com.sentinel.ledger.dto.UserDto;
import com.sentinel.ledger.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Current authenticated user profile management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserDto> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current authenticated user display name only")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}
