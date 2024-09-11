package com.medilocate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilocate.security.dto.SecurityErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        log.error("Unauthorized error: {}", authException.getMessage(), authException);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        SecurityErrorResponse errorResponse = new SecurityErrorResponse(
                new Date(), HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized", authException.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
