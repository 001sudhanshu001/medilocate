package com.medilocate.repository;

import com.medilocate.entity.Doctor;
import com.medilocate.entity.Slot;
import com.medilocate.entity.enums.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId AND s.doctor.id = :doctorId AND s.status = :status")
    Optional<Slot> findByDoctorIdAndIdAndStatusWithLock(Long doctorId, Long slotId, SlotStatus status);

    @Modifying
    @Query("Update Slot s Set s.status = ?1 where s.id = ?2")
    void updateSlotStatus(SlotStatus status, Long slotId);

    @Query("SELECT s FROM Slot s WHERE s.doctor = :doctor AND (s.startTime < :endTime AND s.endTime > :startTime)")
    List<Slot> findByDoctorAndTimeRange(@Param("doctor") Doctor doctor,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Slot s WHERE s.doctor = :doctor AND s.startTime >= :currentTime")
    List<Slot> findFutureSlotsOfDoctor(@Param("doctor") Doctor doctor,
                                       @Param("currentTime") LocalDateTime currentTime);


    @Query("SELECT s FROM Slot s WHERE s.id = :slotId AND s.doctor.id = :doctorId") // AND s.status = :status
    Optional<Slot> findByIdAndDoctorId(Long doctorId, Long slotId);

    @Query("SELECT s FROM Slot s WHERE s.doctor.id = ?1 " +
            "AND FUNCTION('DATE', s.startTime) = ?2") //AND s.isDeleted = false
    List<Slot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate date);
}