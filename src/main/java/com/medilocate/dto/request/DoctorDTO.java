package com.medilocate.dto.request;

import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDTO {

    @NotBlank(message = "Name can't be Blank")
    @Max(value = 50, message = "Name Must be between 1 to 50 Characters")
    private String name;

    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    private Map<String, String> availability = new LinkedHashMap<>();

    private double latitude;
    private double longitude;

    @NotBlank(message = "City cannot be empty")
    private String city;

    @Enumerated(EnumType.STRING) // For current status, like Occupied, Available
    private DoctorStatus status;
}
