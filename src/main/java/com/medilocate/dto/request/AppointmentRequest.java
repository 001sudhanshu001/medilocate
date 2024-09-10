package com.medilocate.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Slot ID cannot be null")
    private Long slotId;

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    // TODO -> Will Switch it with JWT
   // @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Size(max = 50, message = "Patient name cannot exceed 50 characters")
    private String patientName;

    private Integer patientAge;

    private String description;
}
