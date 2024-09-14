package com.medilocate.controller;

import com.medilocate.dto.request.DoctorSlotConfigDTO;
import com.medilocate.entity.DoctorSlotConfiguration;
import com.medilocate.service.AuthenticationService;
import com.medilocate.service.DoctorSlotConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slot-configuration")
@RequiredArgsConstructor
@Slf4j
public class DoctorSlotConfigurationController {

    private final DoctorSlotConfigurationService slotConfigurationService;
    private final AuthenticationService authenticationService;

    @PostMapping("/set")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<String> setSlotConfiguration(@RequestBody DoctorSlotConfigDTO configDTO) {
        if (configDTO.getEndTime().isBefore(configDTO.getStartTime()) ||
                configDTO.getEndTime().equals(configDTO.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be strictly after start time.");
        }

        String doctorEmail = authenticationService.getAuthenticatedUserName();

        log.info("Creating slots-config for {}", doctorEmail);
        String response = slotConfigurationService.saveSlotConfiguration(configDTO, doctorEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public List<DoctorSlotConfigDTO>  getSlots() {

        String doctorEmail = authenticationService.getAuthenticatedUserName();

        log.info("Get slot-config for doctor {}", doctorEmail);
        List<DoctorSlotConfiguration> config = slotConfigurationService.getConfig(doctorEmail);

        return config.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DoctorSlotConfigDTO toDTO(DoctorSlotConfiguration entity) {
        DoctorSlotConfigDTO dto = new DoctorSlotConfigDTO();
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSlotDurationInMinutes(entity.getSlotDurationInMinutes());
        return dto;
    }
}
