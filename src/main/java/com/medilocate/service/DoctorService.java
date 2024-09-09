package com.medilocate.service;

import com.medilocate.dto.request.DoctorDTO;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

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
    public List<Doctor> findClosestDoctors(double userLatitude, double userLongitude, Specialty specialty,
                                           double radius, int page, int size) {

        List<Object[]> closestDoctors = doctorRepository
                        .findClosestDoctors(userLatitude, userLongitude, specialty,
                                radius, PageRequest.of(page - 1, size));

        return closestDoctors.stream()
                .map(result -> {
                    Doctor doctor = (Doctor) result[0];
                    double distance = (Double) result[1];
                    doctor.setDistance(distance);
                    return doctor;
                })
                .sorted(Comparator.comparingDouble(Doctor::getDistance))
                .collect(Collectors.toList());

    }

    @Transactional
    public List<Doctor> searchDoctorsByName(String name, int page, int size,
                                            Double userLatitude, Double userLongitude) {
        List<Doctor> doctorList = doctorRepository
                .findByNameContaining(name, PageRequest.of(page - 1, size));

        if (userLatitude != null && userLongitude != null) {
            calculateDistances(doctorList, userLatitude, userLongitude);
        }
        return doctorList;
    }

    @Transactional
    public List<Doctor> findByCityAndSpeciality(String city, Specialty specialty, int page, int size,
                                                Double userLatitude, Double userLongitude) {

        List<Doctor> doctorList = doctorRepository
                .findByCityIgnoreCaseAndSpecialty(city, specialty, PageRequest.of(page - 1, size));

        if (userLatitude != null && userLongitude != null) {
            calculateDistances(doctorList, userLatitude, userLongitude);
        }
        return doctorList;
    }

    // Or this Calculations can be Done at the DB
    public void calculateDistances(List<Doctor> doctors, double userLatitude, double userLongitude) {
        for (Doctor doctor : doctors) {
            double distance = DistanceUtil.calculateDistance(userLatitude, userLongitude, doctor.getLatitude(), doctor.getLongitude());
            doctor.setDistance(distance);
        }
        doctors.sort(Comparator.comparingDouble(Doctor::getDistance));
    }

}
