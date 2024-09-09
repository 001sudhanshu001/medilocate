package com.medilocate.controller;

import com.medilocate.dto.request.SlotRequest;
import com.medilocate.entity.Slot;
import com.medilocate.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;


    // TODO
   // @PostMapping("/resetSlots")
    public ResponseEntity<String> resetSlots() {
        String usernmae = "sarya@gmail.com"; // Fetch username from JWT
//        slotService.resetSlots(usernmae);
        return ResponseEntity.ok("Slots reset successfully");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSlot(@Valid @RequestBody SlotRequest slotRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid slot data");
        }

        if (!slotRequest.getEndTime().isAfter(slotRequest.getStartTime())) {
            return ResponseEntity.badRequest().body("End time must be after start time");
        }

        String doctorEmail = "sarya@gmail.com"; // FROM JWT
        try{
            slotService.createSlot(slotRequest, doctorEmail);
        }catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return new ResponseEntity<>("Slot Created Successfully", HttpStatus.CREATED);
    }

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
        try{
            slotService.updateSlot(slotRequest, id, doctorEmail);
        }catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return new ResponseEntity<>("Slot Updated Successfully", HttpStatus.OK);
    }

}
