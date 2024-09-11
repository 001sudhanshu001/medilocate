package com.medilocate.service;

import com.medilocate.dto.request.CreateDoctorRequest;
import com.medilocate.dto.response.DoctorResponseDTO;
import com.medilocate.dto.response.DoctorSearchResponse;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.User;
import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Role;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.UserRepository;
import com.medilocate.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Doctor findByEmail(String doctorEmail) {
        return doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not Found"));
    }

    @Transactional
    public Page<Doctor> findAll(int page, int size) {
        return doctorRepository.findAll(PageRequest.of(page - 1, size));
    }

    @Transactional
    public Doctor findDoctorById(Long id, Double userLatitude, Double userLongitude) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Doctor Found with the given Id"));

        if (userLatitude != null && userLongitude != null) {
            double distance = DistanceUtil
                    .calculateDistance(userLatitude, userLongitude, doctor.getLatitude(), doctor.getLongitude());
            doctor.setDistance(distance);
        } else {
            doctor.setDistance(null);
        }

        return doctor;
    }

    @Transactional
    public Doctor saveDoctor(CreateDoctorRequest createDoctorRequest, String createdBy) {
        Doctor doctor = new Doctor();
        doctor.setName(createDoctorRequest.getName());
        doctor.setEmail(createDoctorRequest.getEmail());
        doctor.setHospital(createDoctorRequest.getHospital());
        doctor.setCity(createDoctorRequest.getCity());
        doctor.setSpecialty(createDoctorRequest.getSpecialty());
        doctor.setLatitude(createDoctorRequest.getLatitude());
        doctor.setLongitude(createDoctorRequest.getLongitude());
        doctor.setStatus(DoctorStatus.AVAILABLE);
        doctor.setAvailability(createDoctorRequest.getAvailability());
        doctor.setPhone(createDoctorRequest.getPhone());
        doctor.setCreatedByAdmin(createdBy);
        doctor.setUpdatedByAdmin(createdBy);

        // Create a Corresponding user for the Doctor
        User appUser = User.builder()
                .name(createDoctorRequest.getName())
                .email(createDoctorRequest.getEmail())
                .password(passwordEncoder.encode(createDoctorRequest.getPassword()))
                .phone(createDoctorRequest.getPhone())
                .role(Role.DOCTOR)
                .build();

        userRepository.save(appUser);

        Doctor save = doctorRepository.save(doctor);
        System.out.println("THE DOCTOR ID IS "  + save.getId());
        return save;
    }

    @Transactional
    public Doctor updateDoctor(Long id, CreateDoctorRequest updatedDoctor, String adminEmail) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Doctor Found with the given Id"));

        // Do not update email
        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setHospital(updatedDoctor.getHospital());
        existingDoctor.setSpecialty(updatedDoctor.getSpecialty());
        existingDoctor.setCity(updatedDoctor.getCity());
        existingDoctor.setLatitude(updatedDoctor.getLatitude());
        existingDoctor.setLongitude(updatedDoctor.getLongitude());
        existingDoctor.setAvailability(updatedDoctor.getAvailability());
        existingDoctor.setUpdatedByAdmin(adminEmail);

        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public DoctorSearchResponse findClosestDoctors(double userLatitude, double userLongitude, Specialty specialty,
                                           double radius, int page, int size) {

        Page<Object[]> doctorPage = doctorRepository
                        .findClosestDoctors(userLatitude, userLongitude, specialty,
                                radius, PageRequest.of(page - 1, size));

        List<Object[]> list = new ArrayList<>(doctorPage.getContent());
        List<Doctor> doctorList = list.stream()
                .map(result -> {
                    Doctor doctor = (Doctor) result[0];
                    double distance = (Double) result[1];
                    doctor.setDistance(distance);
                    return doctor;
                })
                .sorted(Comparator.comparingDouble(Doctor::getDistance))
                .toList();

        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();

        return new DoctorSearchResponse(responseDTOS, doctorPage.getTotalPages());
    }

    @Transactional
    public DoctorSearchResponse searchDoctorsByName(String name, int page, int size,
                                            Double userLatitude, Double userLongitude) {

        // TODO : Code Refactor to use
        Page<Doctor> doctorPage = doctorRepository
                .findByNameContaining(name, PageRequest.of(page - 1, size));

        List<Doctor> doctorList = new ArrayList<>(doctorPage.getContent());

        // TODO : Refactor code to Move this Logic to Controller Layer
        if (userLatitude != null && userLongitude != null) {
            calculateDistances(doctorList, userLatitude, userLongitude, false);
        }

        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();

        return new DoctorSearchResponse(responseDTOS, doctorPage.getTotalPages());
    }

    @Transactional
    public DoctorSearchResponse findByCityAndSpeciality(String city, Specialty specialty, int page, int size,
                                                Double userLatitude, Double userLongitude) {

        Page<Doctor> doctorPage = doctorRepository
                .findByCityIgnoreCaseAndSpecialty(city, specialty, PageRequest.of(page - 1, size));

        List<Doctor> doctorList = new ArrayList<>(doctorPage.getContent());

        // TODO : Refactor code to Move this Logic to Controller Layer
        if(doctorList.isEmpty()) {
            return new DoctorSearchResponse(new ArrayList<DoctorResponseDTO>(), 0);
        }

        if (userLatitude != null && userLongitude != null) {
            calculateDistances(doctorList, userLatitude, userLongitude, true);
        }

        List<DoctorResponseDTO> responseDTOS = doctorList.stream()
                .map(this::convertToDoctorResponseDTO)
                .toList();
        return new DoctorSearchResponse(responseDTOS, doctorPage.getTotalPages());
    }

    @Transactional
    // Or this Calculations can be Done at the DB
    public void calculateDistances(List<Doctor> doctors, double userLatitude,
                                   double userLongitude, boolean sortPerDistance) {
        for (Doctor doctor : doctors) {
            Double distance = DistanceUtil.calculateDistance(userLatitude, userLongitude,
                    doctor.getLatitude(), doctor.getLongitude());
            doctor.setDistance(distance);
        }

        if(sortPerDistance && doctors.size() > 1) {
             doctors.sort(Comparator.comparingDouble(Doctor::getDistance));
        }
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


}
