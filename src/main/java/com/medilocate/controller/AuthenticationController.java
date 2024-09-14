package com.medilocate.controller;

import com.medilocate.dto.request.RefreshTokenRequest;
import com.medilocate.security.dto.JwtAuthenticationResponse;
import com.medilocate.security.dto.LogOutRequest;
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

        log.info("User Signup for user {} ", request.getEmail() );

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

        log.info("{} User tried to login", request.getUserName());
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(request);
        log.info(request.getUserName() + " tried to login");

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authenticationService.refresh(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody LogOutRequest logOutRequest) {
        log.info("User logout request to login");
        String userName = authenticationService.logout(logOutRequest);

        return ResponseEntity.ok("User has successfully logged out from the system!");
    }
}
