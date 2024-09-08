package com.medilocate.service;

import com.medilocate.dto.request.DoctorDTO;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.enums.Specialty;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor saveDoctor(DoctorDTO doctorDTO) {
        Doctor doctor = new Doctor();
        doctor.setName(doctorDTO.getName());
        doctor.setCity(doctorDTO.getCity());
        doctor.setSpecialty(doctorDTO.getSpecialty());
        doctor.setLatitude(doctorDTO.getLatitude());
        doctor.setLongitude(doctorDTO.getLongitude());
        doctor.setStatus(doctorDTO.getStatus());
        doctor.setAvailability(doctorDTO.getAvailability());

        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, DoctorDTO updatedDoctor) {
        Doctor existingDoctor = doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("No Doctor Found with the given Id"));

        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setSpecialty(updatedDoctor.getSpecialty());
        existingDoctor.setCity(updatedDoctor.getCity());
        existingDoctor.setLatitude(updatedDoctor.getLatitude());
        existingDoctor.setLongitude(updatedDoctor.getLongitude());
        existingDoctor.setAvailability(updatedDoctor.getAvailability());

        return doctorRepository.save(existingDoctor);
    }

    public List<Doctor> findClosestDoctors(double userLatitude, double userLongitude, Specialty specialty, double radius) {
        List<Object[]> closestDoctors = doctorRepository.findClosestDoctors(userLatitude, userLongitude, specialty, radius);

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

    public List<Doctor> searchDoctorsByName(String name, int page, int size,  Double userLatitude, Double userLongitude) {
        List<Doctor> doctorList = doctorRepository.findByNameContaining(name, PageRequest.of(page - 1, size));

        if (userLatitude != null && userLongitude != null) {
            calculateDistances(doctorList, userLatitude, userLongitude);
        }
        return doctorList;
    }

    public List<Doctor> findByCityAndSpeciality(String city, Specialty specialty, int page, int size,
                                                Double userLatitude, Double userLongitude) {

        List<Doctor> doctorList = doctorRepository.findByCityIgnoreCaseAndSpecialty(city, specialty, PageRequest.of(page - 1, size));

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
