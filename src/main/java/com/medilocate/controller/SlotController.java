package com.medilocate.controller;

import com.medilocate.dto.request.SlotRequest;
import com.medilocate.dto.response.SlotResponse;
import com.medilocate.entity.Slot;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    // DOCTOR ONLY
    @PostMapping("/create")
    public ResponseEntity<?> createSlot(@Valid @RequestBody SlotRequest slotRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid slot data");
        }

        if (!slotRequest.getEndTime().isAfter(slotRequest.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be after start time");
        }

        String doctorEmail = "sarya@gmail.com"; // FROM JWT
        slotService.createSlot(slotRequest, doctorEmail);

        return new ResponseEntity<>("Slot Created Successfully", HttpStatus.CREATED);
    }

    // DOCTOR ONLY
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateSlot(@Valid @RequestBody SlotRequest slotRequest,
                                        BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid slot data");
        }

        if (!slotRequest.getEndTime().isAfter(slotRequest.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be after start time");
        }

        String doctorEmail = "sarya@gmail.com"; // FROM JWT
        slotService.updateSlot(slotRequest, id, doctorEmail);

        return new ResponseEntity<>("Slot Updated Successfully", HttpStatus.OK);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getSlotsByDoctorAndDate(@PathVariable Long doctorId,
                                              @RequestParam("date") String date) {

        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date); // date is passed in YYYY-MM-DD format
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use YYYY-MM-DD");
        }

        List<Slot> slotsByDoctorAndDate = slotService.getSlotsByDoctorAndDate(doctorId, localDate);

        if (slotsByDoctorAndDate.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<SlotResponse> response = slotsByDoctorAndDate.stream()
                .map(this::convertToSlotResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // FOR DOCTOR ONLY
    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long slotId) {
        String doctorEmail = "sarya@gmail.com"; // Will be FROM JWT

        slotService.deleteSlot(slotId, doctorEmail);
        return ResponseEntity.ok("Slot deleted successfully.");
    }

    private SlotResponse convertToSlotResponse(Slot slot) {
        return SlotResponse.builder()
                .slotId(slot.getId())
                .doctorId(slot.getDoctor().getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .build();
    }

    // TODO
    // @PostMapping("/resetSlots")
    public ResponseEntity<String> resetSlots() {
        String usernmae = "sarya@gmail.com"; // Fetch username from JWT
//        slotService.resetSlots(usernmae);
        return ResponseEntity.ok("Slots reset successfully");
    }

}
