package com.medilocate.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninRequest {

    @NotBlank
    private String userName;

    @NotBlank
    private String password;
}