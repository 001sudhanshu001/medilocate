package com.medilocate.controller;

import com.medilocate.dto.request.AppointmentRequest;
import com.medilocate.dto.response.AppointmentResponse;
import com.medilocate.dto.response.BookedAppointmentResponse;
import com.medilocate.entity.Appointment;
import com.medilocate.service.AppointmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<String> bookAppointment(@RequestBody @Valid
                                                      AppointmentRequest appointmentRequest) {
        String username = "user@gmail.com"; // TODO : FROM JWT
        appointmentService.bookAppointment(appointmentRequest, username);
        return ResponseEntity.ok("Appointment booked successfully.");
    }

    @GetMapping("/user")
    public ResponseEntity<AppointmentResponse> getUserAppointments(@RequestParam(defaultValue = "1", required = false)
            @Min(value = 1, message = "Page must be greater than or equal to 1") int page) {

        String userEmail = "user@gmail.com"; // TODO : From JWT

        AppointmentResponse response = appointmentService.getAppointmentsByUser(userEmail, page);
        if(response.getAppointmentList().isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(response);
//        List<Appointment> appointmentList =
//                appointmentService.getAppointmentsByUser(userEmail, page).getContent();
//
//        if(appointmentList.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//
//        List<BookedAppointmentResponse> responseList = appointmentList.stream()
//                .map(this::convertToResponse)
//                .toList();
//
//        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/doctor")
    public ResponseEntity<AppointmentResponse> getDoctorAppointments(
            @RequestParam(required = false) String date, // YYYY-MM-DD
            @RequestParam(defaultValue = "1", required = false)
            @Min(value = 1, message = "Page must be greater than or equal to 1") int page) {

        String doctorEmail = "sarya@gmail.com"; // TODO : From JWT

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

        String userEmail = "user@gmail.com"; // TODO : From JWT
        try {
            appointmentService.cancelAppointment(appointmentId, userEmail);
            return ResponseEntity.ok("Appointment cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DOCTOR ONLY
    @PostMapping("/complete") // Will be Updated by the Doctor
    public ResponseEntity<String> completeAppointment(@RequestParam Long appointmentId) {

        String doctorEmail = "sarya@gmail.com"; // TODO : FROM JWT
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
