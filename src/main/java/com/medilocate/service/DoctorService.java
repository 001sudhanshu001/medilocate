package com.medilocate.service;

import com.medilocate.dto.request.DoctorDTO;
import com.medilocate.dto.response.DoctorResponseDTO;
import com.medilocate.dto.response.DoctorSearchResponse;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional
    public Doctor findByEmail(String doctorEmail) {
        return doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not Found"));
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
    public Doctor saveDoctor(DoctorDTO doctorDTO) {
        Doctor doctor = new Doctor();
        doctor.setName(doctorDTO.getName());
        doctor.setHospital(doctorDTO.getHospital());
        doctor.setCity(doctorDTO.getCity());
        doctor.setSpecialty(doctorDTO.getSpecialty());
        doctor.setLatitude(doctorDTO.getLatitude());
        doctor.setLongitude(doctorDTO.getLongitude());
        doctor.setStatus(doctorDTO.getStatus());
        doctor.setAvailability(doctorDTO.getAvailability());

        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Long id, DoctorDTO updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Doctor Found with the given Id"));

        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setHospital(updatedDoctor.getHospital());
        existingDoctor.setSpecialty(updatedDoctor.getSpecialty());
        existingDoctor.setCity(updatedDoctor.getCity());
        existingDoctor.setLatitude(updatedDoctor.getLatitude());
        existingDoctor.setLongitude(updatedDoctor.getLongitude());
        existingDoctor.setAvailability(updatedDoctor.getAvailability());

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

        // TODO : Refactor Code to separate the Distance Calculation Logic
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
