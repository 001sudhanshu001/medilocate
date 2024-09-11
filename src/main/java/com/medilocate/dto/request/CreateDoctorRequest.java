package com.medilocate.dto.request;

import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorRequest {

    @NotBlank(message = "Name can't be Blank")
    @Max(value = 50, message = "Name Must be between 1 to 50 Characters")
    private String name;

    @NotBlank(message = "Hospital/Clinic name can't be Blank")
    @Max(value = 50, message = "Hospital/Clinic name Must be between 1 to 50 Characters")
    private String hospital;

    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    private Map<String, String> availability = new LinkedHashMap<>();

    @NotNull(message = "Latitude cannot be null")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90.0 and 90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90.0 and 90.0")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180.0 and 180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180.0 and 180.0")
    private Double longitude;

    @NotBlank(message = "City cannot be empty")
    private String city;

    @Enumerated(EnumType.STRING)
    private DoctorStatus status;

    @NotBlank(message = "Email is mandatory")
    @Email
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}
