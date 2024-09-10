package com.medilocate.security.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class JwtSecurityException extends RuntimeException {
    private final JWTErrorCode jwtErrorCode;

    public JwtSecurityException(JWTErrorCode jwtErrorCode, String message) {
        super(message);
        this.jwtErrorCode = jwtErrorCode;
    }

    @Getter
    public enum JWTErrorCode {
        USER_NOT_FOUND(HttpServletResponse.SC_NOT_FOUND),
        MAX_SESSION_REACHED(HttpServletResponse.SC_PRECONDITION_FAILED),
        SESSION_NOT_FOUND(HttpServletResponse.SC_BAD_REQUEST),
        REFRESH_TOKEN_EXPIRED(HttpServletResponse.SC_UNAUTHORIZED),
        INVALID_REFRESH_TOKEN(HttpServletResponse.SC_BAD_REQUEST),
        REFRESH_TOKEN_ONLY_ALLOWED_WITH_EXPIRED_TOKEN(HttpServletResponse.SC_BAD_REQUEST);

        public final int errorCode;

        JWTErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public HttpStatusCode httpStatus() {
            return HttpStatus.valueOf(errorCode);
        }
    }
}
