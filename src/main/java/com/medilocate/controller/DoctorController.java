package com.medilocate.controller;

import com.medilocate.dto.request.DoctorDTO;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.service.DoctorService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // ADMIN ONLY
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody DoctorDTO doctorDTO) {
        // TODO : Use DTO
        Doctor savedDoctor = doctorService.saveDoctor(doctorDTO);
        return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    }

    // ADMIN ONLY
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorDTO doctorDTO) {
        Doctor savedDoctor = doctorService.updateDoctor(id, doctorDTO);
        return ResponseEntity.ok(savedDoctor);
    }

    @GetMapping("/search-closest")
    public ResponseEntity<List<Doctor>> searchDoctors(
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude,
            @RequestParam Specialty specialty,
            @RequestParam(defaultValue = "5", required = false) double radius) {
        return ResponseEntity.ok(doctorService.findClosestDoctors(userLatitude, userLongitude, specialty, radius));
    }

    // TODO : Input Validation and Handling Exception
    @GetMapping("/search")
    public ResponseEntity<?> searchDoctorsByName(
        @RequestParam @NotBlank(message = "Name cannot be blank") String name,
        @RequestParam(required = false) double userLatitude,
        @RequestParam(required = false) double userLongitude,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
        @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
        @Max(value = 15, message = "Size must be at most 15") int size) {

        return ResponseEntity.ok(doctorService.searchDoctorsByName(name, page, size, userLatitude, userLongitude));
    }

    // TODO : Input Validation and Handling Exception
    @GetMapping("/search-by-city-and-specialty")
    public ResponseEntity<?> searchDoctorsByCityAndSpeciality(
            @RequestParam @NotBlank(message = "City cannot be blank") String city,
            @RequestParam @NotNull(message = "Specialty is required") Specialty specialty,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {

        return ResponseEntity.ok(doctorService
                .findByCityAndSpeciality(city, specialty, page, size, userLatitude, userLongitude));

    }


    /*********************************** TODO *****************************************/
    // TODO : Review for Doctor
    /*
    @GetMapping("/{doctorId}/appointments")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(appointments);
    }
     */
    // TODO : search doctor by Availability

//    @PutMapping("/{id}")
//    public ResponseEntity<Doctor> updateDoctor(
//            @PathVariable Long id,
//            @RequestBody Doctor updatedDoctor) {
//        Doctor savedDoctor = doctorService.updateDoctor(id, updatedDoctor);
//        return ResponseEntity.ok(savedDoctor);
//    }
}
