package com.medilocate.service;

import com.medilocate.dto.request.SlotRequest;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.Slot;
import com.medilocate.entity.enums.SlotStatus;
import com.medilocate.exception.EntityNotFoundException;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public Slot createSlot(SlotRequest slotRequest, String doctorEmail) {
        // This will never happen as we will implement JWT, so can remove this
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Slot slot = new Slot();
        slot.setStartTime(slotRequest.getStartTime());
        slot.setEndTime(slotRequest.getEndTime());
        slot.setDoctor(doctor);
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setDeleted(false);

        List<Slot> existingSlots = slotRepository.findAllActiveSlotsByDoctor(doctor);
        if (isSlotOverlapping(slot, existingSlots)) {
            throw new IllegalStateException("The slot overlaps with an existing slot.");
        }

        return slotRepository.save(slot);
    }

    @Transactional
    public Slot updateSlot(SlotRequest slotRequest, Long slotId, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Slot slot = slotRepository.findByIdAndDoctorId(slotId, doctor.getId())
                .orElseThrow(() -> new EntityNotFoundException("Slot not found"));

        slot.setStartTime(slotRequest.getStartTime());
        slot.setEndTime(slotRequest.getEndTime());
        slot.setDoctor(doctor);
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setDeleted(false);

        List<Slot> existingSlots = slotRepository.findAllActiveSlotsByDoctor(doctor);
        if (isSlotOverlapping(slot, existingSlots)) {
            throw new IllegalStateException("Slot overlaps with an existing slot");
        }

        return slotRepository.save(slot);
    }

    public List<Slot> getSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndSlotDate(doctorId, date);
    }

    @Transactional
    public void deleteSlot(Long slotId, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"))
                ;
        Slot slot = slotRepository.findByIdAndDoctorId(slotId, doctor.getId())
                .orElseThrow(() -> new EntityNotFoundException("Slot not found"));

        slotRepository.delete(slot);
    }

    private boolean isOverlapping(Slot newSlot, Slot existingSlot) {
        return newSlot.getDoctor().equals(existingSlot.getDoctor()) &&
                !(newSlot.getEndTime().isBefore(existingSlot.getStartTime()) ||
                        newSlot.getStartTime().isAfter(existingSlot.getEndTime()));
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
