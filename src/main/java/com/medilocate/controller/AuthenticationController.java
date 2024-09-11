package com.medilocate.controller;

import com.medilocate.entity.enums.Role;
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

        //TODO : Duplicate Email is handled in Global Exception Handler
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
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(request, Role.PATIENT);

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @PostMapping("/doctor-signin")
    public ResponseEntity<JwtAuthenticationResponse> doctorSignin(
            @RequestBody @Valid SigninRequest request) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(request, Role.DOCTOR);

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }
    @PostMapping("/admin-signin")
    public ResponseEntity<JwtAuthenticationResponse> adminSignin(
            @RequestBody @Valid SigninRequest request) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(request, Role.ADMIN);

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @PostMapping("/super-signin")
    public ResponseEntity<JwtAuthenticationResponse> superAdmin(
            @RequestBody @Valid SigninRequest request) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(request, Role.SUPER_ADMIN);

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody LogOutRequest logOutRequest) {
        String userName = authenticationService.logout(logOutRequest);

        return ResponseEntity.ok("User has successfully logged out from the system!");
    }
}
