package com.medilocate.service;

import com.medilocate.dto.request.SlotRequest;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.Slot;
import com.medilocate.entity.enums.SlotStatus;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.exception.custom.SlotNotFoundException;
import com.medilocate.exception.custom.SlotOverlapException;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public Slot createSlot(SlotRequest slotRequest, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Slot slot = new Slot();
        slot.setStartTime(slotRequest.getStartTime());
        slot.setEndTime(slotRequest.getEndTime());
        slot.setDoctor(doctor);
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setDeleted(false);

        List<Slot> existingSlots = slotRepository.findFutureSlotsOfDoctor(doctor, LocalDateTime.now());
        if (isSlotOverlapping(slot, existingSlots)) {
            throw new SlotOverlapException("The slot overlaps with an existing slot.");
        }

        return slotRepository.save(slot);
    }

    @Transactional
    public Slot updateSlot(SlotRequest slotRequest, Long slotId, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Slot slot = slotRepository.findByIdAndDoctorId(slotId, doctor.getId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        slot.setStartTime(slotRequest.getStartTime());
        slot.setEndTime(slotRequest.getEndTime());
        slot.setDoctor(doctor);
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setDeleted(false);

        List<Slot> existingSlots = slotRepository.findFutureSlotsOfDoctor(doctor, LocalDateTime.now());
        if (isSlotOverlapping(slot, existingSlots)) {
            throw new SlotOverlapException("Slot overlaps with an existing slot");
        }

        return slotRepository.save(slot);
    }

    @Transactional
    public List<Slot> getSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndSlotDate(doctorId, date);
    }

    @Transactional
    public void deleteSlot(Long slotId, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Slot slot = slotRepository.findByIdAndDoctorId(slotId, doctor.getId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        slotRepository.delete(slot);
    }

    private boolean isSlotOverlapping(Slot newSlot, List<Slot> existingSlots) {
        return existingSlots.stream()
                .anyMatch(existingSlot ->
                        newSlot.getStartTime().isBefore(existingSlot.getEndTime()) &&
                                newSlot.getEndTime().isAfter(existingSlot.getStartTime()) &&
                                !existingSlot.getId().equals(newSlot.getId())
                );
    }

}
