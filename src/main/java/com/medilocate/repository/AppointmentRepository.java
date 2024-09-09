package com.medilocate.repository;

import com.medilocate.entity.Appointment;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
//    List<Appointment> findByDoctorAndStartTime(Doctor doctor, LocalDateTime startTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.slot.startTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByDoctorAndStartTimeBetweenForUpdate(@Param("doctor") Doctor doctor,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    List<Appointment> findByBookedBY(User user);

}