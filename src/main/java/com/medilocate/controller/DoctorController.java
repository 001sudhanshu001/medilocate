package com.medilocate.controller;

import com.medilocate.dto.request.DoctorDTO;
import com.medilocate.dto.response.DoctorResponseDTO;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable Long id,
                                       @RequestParam(required = false) Double userLatitude,
                                       @RequestParam(required = false) Double userLongitude
    ) {
        Doctor doctor = doctorService.findDoctorById(id, userLatitude, userLongitude);
        System.out.println("DOCTOR DISTANCE :: " + doctor.getDistance());
        return ResponseEntity.ok(convertToDoctorResponseDTO(doctor));
    }

    @GetMapping("/search") // This is Used for Search Bar
    public ResponseEntity<?> searchDoctorsByName(
            @RequestParam @NotBlank(message = "Name cannot be blank") String name,
            @RequestParam(required = false) double userLatitude,
            @RequestParam(required = false) double userLongitude,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {

        List<Doctor> doctorList = doctorService.searchDoctorsByName(name, page, size, userLatitude, userLongitude);
        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();

        return ResponseEntity.ok(responseDTOS);
    }

    @GetMapping("/search-closest")
    public ResponseEntity<List<?>> searchDoctors(
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude,
            @RequestParam Specialty specialty,
            @RequestParam(defaultValue = "5", required = false) double radius,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {

        List<Doctor> doctorList = doctorService
                .findClosestDoctors(userLatitude, userLongitude, specialty, radius, page, size);
        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();

        return ResponseEntity.ok(responseDTOS);
    }

    // TODO : Input Validation and Handling Exception
    @GetMapping("/search-by-city-and-specialty")
    public ResponseEntity<List<?>> searchDoctorsByCityAndSpeciality(
            @RequestParam @NotBlank(message = "City cannot be blank") String city,
            @RequestParam @NotNull(message = "Specialty is required") Specialty specialty,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {

        List<Doctor> doctorList = doctorService
                .findByCityAndSpeciality(city, specialty, page, size, userLatitude, userLongitude);

        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();

        return ResponseEntity.ok(responseDTOS);

    }

    public DoctorResponseDTO convertToDoctorResponseDTO(Doctor doctor) {
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .hospital(doctor.getHospital())
                .specialty(doctor.getSpecialty())
                .availability(doctor.getAvailability())
                .latitude(doctor.getLatitude())
                .longitude(doctor.getLongitude())
                .city(doctor.getCity())
                .status(doctor.getStatus())
                .distance(doctor.getDistance())
                .email(doctor.getEmail())
                .build();
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
