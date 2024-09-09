package com.medilocate.service;

import com.medilocate.dto.request.AppointmentRequest;
import com.medilocate.entity.Appointment;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.Slot;
import com.medilocate.entity.User;
import com.medilocate.entity.enums.AppointmentStatus;
import com.medilocate.entity.enums.SlotStatus;
import com.medilocate.repository.AppointmentRepository;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.SlotRepository;
import com.medilocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    // TODO : Appointment can be booked for let say at max 30 day before only

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;

    // TODO : Use Proper Exceptions, Like SlotNotAvailableException, DoctorNotFound etc...
    @Transactional
    public void bookAppointment(AppointmentRequest request, String username) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User Not Found !!!"));

        Slot slot = slotRepository.findByDoctorIdAndIdAndStatusWithLock(
                request.getDoctorId(),
                request.getSlotId(),
                SlotStatus.AVAILABLE
        ).orElseThrow(() -> new IllegalStateException("No available slot found for the specified doctor and slot ID"));

        if (!slot.getDoctor().equals(doctor)) {
            throw new IllegalStateException("The slot does not belong to the specified doctor");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(slot.getStartTime())) { // TODO : We can add some Time Relaxation ,like if slot is empty
            // and some time is remaining for the slot then it can be booked
            if (slot.getStatus() == SlotStatus.AVAILABLE) {
                slotRepository.updateSlotStatus(SlotStatus.BOOKED, slot.getId());
            } else {
                throw new IllegalStateException("Slot is already booked");
            }
        } else {
            throw new IllegalStateException("Slot cannot be booked as it is in the past");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setBookedBY(user);
        appointment.setSlot(slot);
        appointment.setDescription(request.getDescription());
        appointment.setAppointmentStatus(AppointmentStatus.BOOKED);

        appointmentRepository.save(appointment);
    }

    // TODO : Appointment Cancellation

}
