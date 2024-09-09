package com.medilocate.service;

import com.medilocate.dto.request.AppointmentRequest;
import com.medilocate.entity.Appointment;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.Slot;
import com.medilocate.entity.User;
import com.medilocate.entity.enums.AppointmentStatus;
import com.medilocate.entity.enums.SlotStatus;
import com.medilocate.exception.custom.EntityNotFoundException;
import com.medilocate.exception.custom.UserNotFoundException;
import com.medilocate.repository.AppointmentRepository;
import com.medilocate.repository.DoctorRepository;
import com.medilocate.repository.SlotRepository;
import com.medilocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    // TODO : Appointment can be booked for let say at max 30 day before only

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;

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

    public Page<Appointment> getAppointmentsByUser(String userEmail, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User Not Found !!!"));

        return appointmentRepository.findByBookedBYOrderByIdDesc(user, pageable);
    }

    // TODO : Appointment Cancellation, Appointment Booking and Cancellation Time

    @Transactional
    public void cancelAppointment(Long appointmentId, String userEmail) {
        Appointment appointment = appointmentRepository.findByIdAndUserEmail(appointmentId, userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (appointment.getSlot().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel an appointment that has already occurred");
        }

        // Update Slot as AVAILABLE
        slotRepository.updateSlotStatus(SlotStatus.AVAILABLE, appointment.getSlot().getId());

        appointmentRepository.updateAppointmentStatusToCancel(appointmentId, AppointmentStatus.CANCELLED, LocalDateTime.now());
    }

    @Transactional
    public void completeAppointment(Long appointmentId, String doctorEmail) {
        Appointment appointment = appointmentRepository.findByIdAndDoctorEmail(appointmentId, doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        if (appointment.getSlot().getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot complete an appointment that is in the future");
        }

        // Mark the appointment as completed
        appointmentRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);

        slotRepository.updateSlotStatus(SlotStatus.USED, appointment.getSlot().getId());
    }

}
