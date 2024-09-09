package com.medilocate.repository;

import com.medilocate.entity.Appointment;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.User;
import com.medilocate.entity.enums.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
//    List<Appointment> findByDoctorAndStartTime(Doctor doctor, LocalDateTime startTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.slot.startTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByDoctorAndStartTimeBetweenForUpdate(@Param("doctor") Doctor doctor,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    List<Appointment> findByBookedBY(User user);

    Page<Appointment> findByBookedBYOrderByIdDesc(User user, Pageable pageable);

    Optional<Appointment> findByIdAndBookedBY(Long id, User bookedBy);

    Optional<Appointment> findByIdAndDoctor(Long id, Doctor doctor);

    @Query("SELECT a FROM Appointment a WHERE a.id = ?1 and a.doctor.email = ?2")
    Optional<Appointment> findByIdAndDoctorEmail(Long id, String email);

    @Query("SELECT a FROM Appointment a WHERE a.id = ?1 AND a.bookedBY.email = ?2")
    Optional<Appointment> findByIdAndUserEmail(Long appointmentId, String email);

    @Modifying
    @Query("UPDATE Appointment a SET a.appointmentStatus = ?2 WHERE a.id = ?1")
    void updateAppointmentStatus(Long appointmentId, AppointmentStatus appointmentStatus);

}