package com.medilocate.controller;

import com.medilocate.dto.request.AppointmentRequest;
import com.medilocate.dto.response.AppointmentResponse;
import com.medilocate.dto.response.BookedAppointmentResponse;
import com.medilocate.entity.Appointment;
import com.medilocate.service.AppointmentService;
import com.medilocate.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthenticationService authenticationService;

    @PostMapping("/book")
    public ResponseEntity<String> bookAppointment(@RequestBody @Valid
                                                      AppointmentRequest appointmentRequest) {
        String userEmail = authenticationService.getAuthenticatedUserName();
        appointmentService.bookAppointment(appointmentRequest, userEmail);
        return ResponseEntity.ok("Appointment booked successfully.");
    }

    @GetMapping("/user")
    public ResponseEntity<AppointmentResponse> getUserAppointments(@RequestParam(defaultValue = "1", required = false)
            @Min(value = 1, message = "Page must be greater than or equal to 1") int page) {

        String username = authenticationService.getAuthenticatedUserName();

        AppointmentResponse response = appointmentService.getAppointmentsByUser(username, page);
        if(response.getAppointmentList().isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<AppointmentResponse> getDoctorAppointments(
            @RequestParam(required = false) String date, // YYYY-MM-DD
            @RequestParam(defaultValue = "1", required = false)
            @Min(value = 1, message = "Page must be greater than or equal to 1") int page) {

        String doctorEmail = authenticationService.getAuthenticatedUserName();

        AppointmentResponse appointmentResponse;
        if(date == null) {
            appointmentResponse = appointmentService.getAppointmentsByDoctor(doctorEmail, LocalDate.now(), page);
        } else {
            LocalDate localDate = LocalDate.parse(date);

            appointmentResponse = appointmentService.getAppointmentsByDoctor(doctorEmail, localDate, page);
        }
        if (appointmentResponse.getAppointmentList().isEmpty()) {
            return new ResponseEntity<>(appointmentResponse, HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(appointmentResponse);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelAppointment(@RequestParam Long appointmentId) {

        String userEmail = authenticationService.getAuthenticatedUserName();
        try {
            appointmentService.cancelAppointment(appointmentId, userEmail);
            return ResponseEntity.ok("Appointment cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/complete") // Will be Updated by the Doctor
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<String> completeAppointment(@RequestParam Long appointmentId) {

        String doctorEmail = authenticationService.getAuthenticatedUserName();
        try {
            appointmentService.completeAppointment(appointmentId, doctorEmail);
            return ResponseEntity.ok("Appointment marked as completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private BookedAppointmentResponse convertToResponse(Appointment appointment) {
        return BookedAppointmentResponse.builder()
                .id(appointment.getId())
                .doctorName(appointment.getDoctor().getName())
                .city(appointment.getDoctor().getCity())
                .latitude(appointment.getDoctor().getLatitude())
                .longitude(appointment.getDoctor().getLongitude())
                .appointmentStatus(appointment.getAppointmentStatus())
                .startTime(appointment.getSlot().getStartTime())
                .endTime(appointment.getSlot().getEndTime())
                .build();
    }

}
