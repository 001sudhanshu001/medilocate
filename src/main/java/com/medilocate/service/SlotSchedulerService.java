package com.medilocate.service;

import com.medilocate.entity.Doctor;
import com.medilocate.entity.DoctorSlotConfiguration;
import com.medilocate.entity.Slot;
import com.medilocate.entity.enums.SlotStatus;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.DoctorSlotConfigurationRepository;
import com.medilocate.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SlotSchedulerService {
    private final DoctorRepository doctorRepository;
    private final DoctorSlotConfigurationRepository configRepository;
    private final SlotRepository slotRepository;

    // TODO :Enhance it using the Availability of Doctor, which can generate Slot for Doctors based on there schedule
    @Scheduled(cron = "0 0 22 ? * SUN-THU")
//    @Scheduled(fixedDelay = 30000)
    public void generateDoctorSlotsForNextDay() {
        List<Doctor> doctors = doctorRepository.findAll();
        LocalDate nextDay = LocalDate.now().plusDays(1);

        System.out.println("Scheduler running");

        for (Doctor doctor : doctors) {
            List<DoctorSlotConfiguration> configs = configRepository.findByDoctor(doctor);
            for (DoctorSlotConfiguration config : configs) {
                generateSlotsForDoctor(doctor, nextDay, config);
            }
        }

    }

    @Transactional
    public void generateSlotsForDoctor(Doctor doctor, LocalDate date, DoctorSlotConfiguration config) {
        LocalTime slotStartTime = config.getStartTime();
        LocalTime slotEndTime = config.getEndTime();

        List<Slot> existingSlots = slotRepository.findByDoctorIdAndSlotDate(doctor.getId(), date);

        if (!isOverlapping(slotStartTime, slotEndTime, existingSlots)) {
            Slot slot = new Slot();
            slot.setDoctor(doctor);
            slot.setStartTime(LocalDateTime.of(date, slotStartTime));
            slot.setEndTime(LocalDateTime.of(date, slotEndTime));
            slot.setStatus(SlotStatus.AVAILABLE);

            slotRepository.save(slot);
        } else {
            // Skip, If overlaps
            System.out.println("Skipping overlapping slot for Doctor: " + doctor.getName() + " on " + date);
        }
    }

    private boolean isOverlapping(LocalTime newSlotStart, LocalTime newSlotEnd, List<Slot> existingSlots) {
        for (Slot existingSlot : existingSlots) {
            LocalTime existingSlotStart = existingSlot.getStartTime().toLocalTime();
            LocalTime existingSlotEnd = existingSlot.getEndTime().toLocalTime();

            if (newSlotStart.isBefore(existingSlotEnd) && newSlotEnd.isAfter(existingSlotStart)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    private boolean isOverlappingOLD(LocalTime newSlotStart, LocalTime newSlotEnd, List<Slot> existingSlots, LocalDate date) {
        for (Slot existingSlot : existingSlots) {
            LocalDateTime existingSlotStart = existingSlot.getStartTime();
            LocalDateTime existingSlotEnd = existingSlot.getEndTime();

            if (existingSlotStart.toLocalDate().equals(date)) {
                if (newSlotStart.isBefore(existingSlotEnd.toLocalTime()) && newSlotEnd.isAfter(existingSlotStart.toLocalTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Deprecated
    @Transactional
    public void generateSlotsForDoctorOLD(Doctor doctor, LocalDate date, DoctorSlotConfiguration config) {
        LocalTime slotTime = config.getStartTime();
        LocalTime endTime = config.getEndTime();

        Slot slot = new Slot();
        slot.setDoctor(doctor);
        slot.setStartTime(LocalDateTime.of(date, slotTime));
        slot.setEndTime(LocalDateTime.of(date, endTime));
        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);
    }
}
