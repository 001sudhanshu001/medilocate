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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
//    List<Appointment> findByDoctorAndStartTime(Doctor doctor, LocalDateTime startTime);

    @Deprecated
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.slot.startTime BETWEEN :startTime AND :endTime")
    List<Appointment> findByDoctorAndStartTimeBetweenForUpdate(@Param("doctor") Doctor doctor,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

    List<Appointment> findByBookedBY(User user);

    @Query("SELECT a FROM Appointment  a WHERE a.bookedBY = ?1 ORDER BY a.id DESC")
    Page<Appointment> findByBookedBYOrderById(User user, Pageable pageable);

    Optional<Appointment> findByIdAndBookedBY(Long id, User bookedBy);

    Optional<Appointment> findByIdAndDoctor(Long id, Doctor doctor);

    @Query("SELECT a FROM Appointment a WHERE a.id = ?1 and a.doctor.email = ?2")
    Optional<Appointment> findByIdAndDoctorEmail(Long id, String email);

    @Query("SELECT a FROM Appointment a WHERE a.id = ?1 AND a.bookedBY.email = ?2")
    Optional<Appointment> findByIdAndUserEmail(Long appointmentId, String email);

    @Modifying
    @Query("UPDATE Appointment a SET a.appointmentStatus = ?2, a.canceledAt = ?3  WHERE a.id = ?1")
    void updateAppointmentStatusToCancel(Long appointmentId, AppointmentStatus appointmentStatus,
                                         LocalDateTime cancelTime);

    @Modifying
    @Query("UPDATE Appointment a SET a.appointmentStatus = ?2  WHERE a.id = ?1")
    void updateAppointmentStatus(Long appointmentId, AppointmentStatus appointmentStatus);
    
    @Query("SELECT a FROM Appointment a WHERE a.notificationSent = false AND a.appointmentStatus = :status " +
            "AND a.startTime BETWEEN :now AND :oneHourLater")
    List<Appointment> findAppointmentsForNotification(
            @Param("now") LocalDateTime now,
            @Param("oneHourLater") LocalDateTime oneHourLater,
            @Param("status") AppointmentStatus status
    );

    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND FUNCTION('DATE', a.startTime) = :date ORDER BY a.id DESC ")
    Page<Appointment> findByDoctorAndDate(@Param("doctor") Doctor doctor,
                                          @Param("date") LocalDate date,
                                          Pageable pageable);

    @Modifying
    @Query("UPDATE Appointment a SET a.notificationSent = true WHERE a.id IN :ids")
    void updateNotificationSentStatus(@Param("ids") List<Long> ids);

    void deleteByStartTimeBefore(LocalDateTime cutoffDate);

}