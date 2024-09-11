package com.medilocate.service;

import com.medilocate.dto.request.DoctorSlotConfigDTO;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.DoctorSlotConfiguration;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.exception.custom.SlotOverlapException;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.DoctorSlotConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DoctorSlotConfigurationService {

    private final DoctorSlotConfigurationRepository configRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public String saveSlotConfiguration(DoctorSlotConfigDTO configDTO, String username) {
        Doctor doctor = doctorRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        if (isOverlapping(configDTO, doctor)) {
            throw new SlotOverlapException("The slot overlaps with an already defined slot");
        }

        DoctorSlotConfiguration config = new DoctorSlotConfiguration();
        config.setDoctor(doctor);
        config.setStartTime(configDTO.getStartTime());
        config.setEndTime(configDTO.getEndTime());
        config.setSlotDurationInMinutes(configDTO.getSlotDurationInMinutes());

        configRepository.save(config);
        return "Configuration saved successfully";
    }

    @Transactional
    public List<DoctorSlotConfiguration> getConfig(String doctorEmail) {
        Doctor doctor = new Doctor();
        doctor.setId(1L);

        List<DoctorSlotConfiguration> existingConfigs = configRepository.findByDoctor(doctor);
        return existingConfigs;
    }
    private boolean isOverlapping(DoctorSlotConfigDTO newConfig, Doctor doctor) {
        List<DoctorSlotConfiguration> existingConfigs = configRepository.findByDoctor(doctor);

        LocalTime newStartTime = newConfig.getStartTime();
        LocalTime newEndTime = newConfig.getEndTime();

        for (DoctorSlotConfiguration existingConfig : existingConfigs) {
            LocalTime existingStartTime = existingConfig.getStartTime();
            LocalTime existingEndTime = existingConfig.getEndTime();

            if (newStartTime.isBefore(existingEndTime) && newEndTime.isAfter(existingStartTime)) {
                return true;
            }
        }

        return false;
    }
}
