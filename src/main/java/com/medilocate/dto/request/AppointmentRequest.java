package com.medilocate.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
//    private Long doctorId;
//
//    @Future(message = "Appointment time must be in the future")
//    private LocalDateTime startTime;
//    private LocalDateTime endTime;
//    private String description;
//
//    private String patientName;
//    private String patientPhone;

    @NotNull(message = "Slot ID cannot be null")
    private Long slotId;

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    // TODO -> Will Switch it with JWT
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private String description;
}
