package com.medilocate.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SigninRequest {

    @NotBlank(message = "Username cannot be blank")
    private String userName;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}