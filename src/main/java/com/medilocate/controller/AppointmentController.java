package com.medilocate.controller;

import com.medilocate.dto.request.AppointmentRequest;
import com.medilocate.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public ResponseEntity<String> bookAppointment(@RequestBody @Valid AppointmentRequest appointmentRequest) {
        try {
            String username = "user@gmail.com";
            appointmentService.bookAppointment(appointmentRequest, username);
            return ResponseEntity.ok("Appointment successfully booked.");
        }catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
