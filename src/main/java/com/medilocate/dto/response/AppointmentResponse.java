package com.medilocate.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private List<BookedAppointmentResponse> appointmentList;
    private int totalPages;
}
