package com.medilocate.controller;

import com.medilocate.dto.request.DoctorSlotConfigDTO;
import com.medilocate.service.AuthenticationService;
import com.medilocate.service.DoctorSlotConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slot-configuration")
@RequiredArgsConstructor
public class DoctorSlotConfigurationController {

    private final DoctorSlotConfigurationService slotConfigurationService;
    private final AuthenticationService authenticationService;

    @PostMapping("/set")
    public ResponseEntity<String> setSlotConfiguration(@RequestBody DoctorSlotConfigDTO configDTO) {
        if (configDTO.getEndTime().isBefore(configDTO.getStartTime()) ||
                configDTO.getEndTime().equals(configDTO.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be strictly after start time.");
        }

        String doctorEmail = authenticationService.getAuthenticatedUserName();
        String response = slotConfigurationService.saveSlotConfiguration(configDTO, doctorEmail);
        return ResponseEntity.ok(response);
    }
}
