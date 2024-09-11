package com.medilocate.controller;

import com.medilocate.dto.request.SlotRequest;
import com.medilocate.dto.response.SlotResponse;
import com.medilocate.entity.Slot;
import com.medilocate.service.AuthenticationService;
import com.medilocate.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private final AuthenticationService authenticationService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> createSlot(@Valid @RequestBody SlotRequest slotRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid slot data");
        }

        if (!slotRequest.getEndTime().isAfter(slotRequest.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be after start time");
        }

        String doctorEmail = authenticationService.getAuthenticatedUserName();
        slotService.createSlot(slotRequest, doctorEmail);

        return new ResponseEntity<>("Slot Created Successfully", HttpStatus.CREATED);
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> updateSlot(@Valid @RequestBody SlotRequest slotRequest,
                                        BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid slot data");
        }

        if (!slotRequest.getEndTime().isAfter(slotRequest.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be after start time");
        }

        String doctorEmail = authenticationService.getAuthenticatedUserName();
        slotService.updateSlot(slotRequest, id, doctorEmail);

        return new ResponseEntity<>("Slot Updated Successfully", HttpStatus.OK);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getSlotsByDoctorAndDate(@PathVariable Long doctorId,
                                              @RequestParam(value = "date", required = false) String date) {

        List<Slot> slotsByDoctorAndDate;
        if(date == null) {
            slotsByDoctorAndDate = slotService.getSlotsByDoctorAndDate(doctorId, LocalDate.now());
        } else {
            LocalDate  localDate = LocalDate.parse(date); // date is passed in YYYY-MM-DD format

            slotsByDoctorAndDate = slotService.getSlotsByDoctorAndDate(doctorId, localDate);
        }
        if (slotsByDoctorAndDate.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<SlotResponse> response = slotsByDoctorAndDate.stream()
                .map(this::convertToSlotResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> deleteSlot(@PathVariable Long slotId) {
        String doctorEmail = authenticationService.getAuthenticatedUserName();

        slotService.deleteSlot(slotId, doctorEmail);
        return ResponseEntity.ok("Slot deleted successfully.");
    }

    private SlotResponse convertToSlotResponse(Slot slot) {
        return SlotResponse.builder()
                .slotId(slot.getId())
                .doctorId(slot.getDoctor().getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus())
                .build();
    }


}
