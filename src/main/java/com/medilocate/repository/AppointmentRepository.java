package com.medilocate.repository;

import com.medilocate.entity.Appointment;
import com.medilocate.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorAndStartTime(Doctor doctor, LocalDateTime startTime);
}