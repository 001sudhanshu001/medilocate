package com.medilocate.controller;

import com.medilocate.constants.AppConstants;
import com.medilocate.dto.request.CreateDoctorRequest;
import com.medilocate.dto.response.DoctorResponseDTO;
import com.medilocate.dto.response.DoctorSearchResponse;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.service.AuthenticationService;
import com.medilocate.service.DoctorService;
import com.medilocate.util.DistanceUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Doctor> createDoctor(@RequestBody CreateDoctorRequest createDoctorRequest) {
        String adminEmail = authenticationService.getAuthenticatedUserName();
        Doctor savedDoctor = doctorService.saveDoctor(createDoctorRequest, adminEmail);
        return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @RequestBody CreateDoctorRequest createDoctorRequest) {
        String adminEmail = authenticationService.getAuthenticatedUserName();
        Doctor savedDoctor = doctorService.updateDoctor(id, createDoctorRequest, adminEmail);
        return ResponseEntity.ok(savedDoctor);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public DoctorSearchResponse getAll(
            @RequestParam(defaultValue = "1", required = false)
            @Min(value = 1, message = "Page must be greater than or equal to 1") Integer page){

        Page<Doctor> doctorPage = doctorService.findAll(page, 5);

        List<Doctor> doctorList = new ArrayList<>(doctorPage.getContent());
        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();

        return new DoctorSearchResponse(responseDTOS, doctorPage.getTotalPages());
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> getProfile() {
        String doctorEmail = authenticationService.getAuthenticatedUserName();
        return ResponseEntity.ok(convertToDoctorResponseDTO(doctorService.findByEmail(doctorEmail)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable Long id,
                                       @RequestParam(required = false) Double userLatitude,
                                       @RequestParam(required = false) Double userLongitude
    ) {
        Doctor doctor = doctorService.findDoctorById(id, userLatitude, userLongitude);
        return ResponseEntity.ok(convertToDoctorResponseDTO(doctor));
    }

    @GetMapping("/search") // This is Used for Search Bar
    public DoctorSearchResponse searchDoctorsByName(
            @RequestParam @NotBlank(message = "Name cannot be blank") String name,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude,
            @RequestParam(defaultValue = "1", required = false) @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {

        DoctorSearchResponse response = doctorService.searchDoctorsByName(name, page, 5, userLatitude, userLongitude);
        return response;
    }


    @GetMapping("/search-closest")
    public DoctorSearchResponse searchDoctors(
            @RequestParam Double userLatitude,
            @RequestParam Double userLongitude,
            @RequestParam Specialty specialty,
            @RequestParam(defaultValue = "5", required = false) double radius,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {


        DoctorSearchResponse closestDoctors = doctorService.findClosestDoctors(userLatitude, userLongitude, specialty, radius, page, size);

        return closestDoctors;
    }

    @GetMapping("/search-by-city-and-specialty")
    public DoctorSearchResponse searchDoctorsByCityAndSpeciality(
            @RequestParam @NotBlank(message = "City cannot be blank") String city,
            @RequestParam @NotNull(message = "Specialty is required") Specialty specialty,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be greater than or equal to 1") int page,
            @RequestParam(defaultValue = "10") @Min(value = 5, message = "Size must be at least 5")
            @Max(value = 15, message = "Size must be at most 15") int size) {

        DoctorSearchResponse searchResponse = doctorService
                .findByCityAndSpeciality(city, specialty, page, size, userLatitude, userLongitude);

//        if(searchResponse.getDoctors().isEmpty()) {
//            return new ResponseEntity<>(searchResponse, HttpStatus.NO_CONTENT);
//        }
//        return ResponseEntity.ok(searchResponse);
        return searchResponse;

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

    public void calculateDistances(List<Doctor> doctors, double userLatitude, double userLongitude) {
        for (Doctor doctor : doctors) {
            double distance = DistanceUtil.calculateDistance(userLatitude, userLongitude, doctor.getLatitude(), doctor.getLongitude());
            doctor.setDistance(distance);
        }
        doctors.sort(Comparator.comparingDouble(Doctor::getDistance));
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


}
