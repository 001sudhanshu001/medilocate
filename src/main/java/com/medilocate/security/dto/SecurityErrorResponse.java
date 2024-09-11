package com.medilocate.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityErrorResponse {
    private final Date timestamp;
    private final int status;
    private final String message;
    private final String details;
}
