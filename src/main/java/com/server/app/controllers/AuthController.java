package com.server.app.controllers;

import com.server.app.config.JsonWebToken;
import com.server.app.dto.auth.AuthResponse;
import com.server.app.dto.auth.LoginDto;
import com.server.app.dto.auth.UpdatePasswordDto;
import com.server.app.dto.auth.UpdateProfileDto;
import com.server.app.dto.user.UserCreateDto;
import com.server.app.entities.User;
import com.server.app.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JsonWebToken jsonWebToken;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDto dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        String token = jsonWebToken.createToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody UserCreateDto dto) {
        User user = userService.signUp(dto);
        String token = jsonWebToken.createToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> profile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileDto dto) {
        User updated = userService.updateProfile(user.getId(), dto);
        String token = jsonWebToken.createToken(updated);
        return ResponseEntity.ok(new AuthResponse(token, updated));
    }

    @PutMapping("/update/password")
    public ResponseEntity<User> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePasswordDto dto) {
        User updated = userService.updatePassword(user.getId(), dto);
        return ResponseEntity.ok(updated);
    }
}