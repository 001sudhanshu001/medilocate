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
@Transactional(readOnly = true)
public class SlotSchedulerService {
    private final DoctorRepository doctorRepository;
    private DoctorSlotConfigurationRepository configRepository;
    private final SlotRepository slotRepository;

    @Scheduled(cron = "0 0 22 ? * SUN-THU")
    public void generateDoctorSlotsForNextDay() {
        List<Doctor> doctors = doctorRepository.findAll();
        LocalDate nextDay = LocalDate.now().plusDays(1);

        for (Doctor doctor : doctors) {
            List<DoctorSlotConfiguration> configs = configRepository.findByDoctor(doctor);
            for (DoctorSlotConfiguration config : configs) {
                generateSlotsForDoctor(doctor, nextDay, config);
            }
        }
    }

    @Transactional
    public void generateSlotsForDoctor(Doctor doctor, LocalDate date, DoctorSlotConfiguration config) {
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
