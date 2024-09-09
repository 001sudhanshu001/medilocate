package com.medilocate.dto.response;

import com.medilocate.entity.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Future;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookedAppointmentResponse {

    private Long id;
    private String doctorName;
    private String city;
    private Double latitude;
    private Double longitude;
    private AppointmentStatus appointmentStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
