package com.medilocate.controller;

import com.medilocate.security.dto.JwtAuthenticationResponse;
import com.medilocate.security.dto.SignUpRequest;
import com.medilocate.security.dto.SigninRequest;
import com.medilocate.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody @Valid SignUpRequest request) {

        // Duplicate Email is handled in Global Exception Handler
        return ResponseEntity.ok(authenticationService.signup(request, false));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(
            @RequestBody @Valid SignUpRequest request) {

        return ResponseEntity.ok(authenticationService.signup(request, true));
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(
            @RequestBody @Valid SigninRequest request) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(request);

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }
}
