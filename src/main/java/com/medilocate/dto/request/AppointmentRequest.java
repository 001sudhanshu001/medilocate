package com.medilocate.dto.request;

import jakarta.validation.constraints.NotNull;
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

    private String description;
}
