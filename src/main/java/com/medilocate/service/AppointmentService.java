package com.medilocate.service;

import com.medilocate.dto.request.AppointmentRequest;
import com.medilocate.dto.response.AppointmentResponse;
import com.medilocate.dto.response.BookedAppointmentResponse;
import com.medilocate.entity.Appointment;
import com.medilocate.entity.Doctor;
import com.medilocate.entity.Slot;
import com.medilocate.entity.User;
import com.medilocate.entity.enums.AppointmentStatus;
import com.medilocate.entity.enums.SlotStatus;
import com.medilocate.exception.custom.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
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
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User Not Found !!!"));

        Slot slot = slotRepository.findByDoctorIdAndIdAndStatusWithLock(
                request.getDoctorId(),
                request.getSlotId(),
                SlotStatus.AVAILABLE
        ).orElseThrow(() -> new SlotNotFoundException("No available slot found for the specified doctor and slot ID"));


        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(slot.getStartTime())) { // TODO : We can add some Time Relaxation ,like if slot is empty
            // and some time is remaining for the slot then it can be booked
            if (slot.getStatus() == SlotStatus.AVAILABLE) {
                slotRepository.updateSlotStatus(SlotStatus.BOOKED, slot.getId());
            } else {
                throw new SlotAlreadyBookedException("Slot is already booked");
            }
        } else {
            throw new SlotExpiredException("Slot cannot be booked as it is in the past");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setBookedBY(user);
        appointment.setPatientName(request.getPatientName());
        appointment.setPatientAge(request.getPatientAge());
        appointment.setSlot(slot);
        appointment.setStartTime(slot.getStartTime());
        appointment.setDescription(request.getDescription());
        appointment.setAppointmentStatus(AppointmentStatus.BOOKED);

        appointmentRepository.save(appointment);
    }

    @Transactional
    public AppointmentResponse getAppointmentsByUser(String userEmail, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User Not Found !!!"));

        Page<Appointment> userAppointmentPage = appointmentRepository.findByBookedBYOrderById(user, pageable);

        ArrayList<Appointment> appointments = new ArrayList<>(userAppointmentPage.getContent());

        List<BookedAppointmentResponse> responseList = appointments.stream()
                .map(this::convertToResponse)
                .toList();

        return new AppointmentResponse(responseList, userAppointmentPage.getTotalPages());
    }

    @Transactional
    public AppointmentResponse getAppointmentsByDoctor(String doctorEmail, LocalDate appointmentDate, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10); // TODO

        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new EntityNotFoundException("Doctor Not Found !!!"));

        Page<Appointment> doctorAppointmentPage =
                appointmentRepository.findByDoctorAndDate(doctor, appointmentDate, pageable);

        List<Appointment> appointments = new ArrayList<>(doctorAppointmentPage.getContent());

        if(appointments.isEmpty()) {
            return new AppointmentResponse(new ArrayList<>(), doctorAppointmentPage.getTotalPages());
        }

        List<BookedAppointmentResponse> responseList = appointments.stream()
                .map(this::convertToResponse)
                .toList();

        return new AppointmentResponse(responseList, doctorAppointmentPage.getTotalPages());
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String userEmail) {
        Appointment appointment = appointmentRepository.findByIdAndUserEmail(appointmentId, userEmail)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with the given Id"));

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
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with the given Id"));

        if (appointment.getSlot().getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot complete an appointment that is in the future");
        }

        // Mark the appointment as completed
        appointmentRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);

        slotRepository.updateSlotStatus(SlotStatus.USED, appointment.getSlot().getId());
    }

    private BookedAppointmentResponse convertToResponse(Appointment appointment) {
        return BookedAppointmentResponse.builder()
                .id(appointment.getId())
                .doctorName(appointment.getDoctor().getName())
                .city(appointment.getDoctor().getCity())
                .latitude(appointment.getDoctor().getLatitude())
                .longitude(appointment.getDoctor().getLongitude())
                .appointmentStatus(appointment.getAppointmentStatus())
                .startTime(appointment.getSlot().getStartTime())
                .endTime(appointment.getSlot().getEndTime())
                .build();
    }

}
