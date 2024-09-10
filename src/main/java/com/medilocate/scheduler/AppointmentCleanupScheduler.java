package com.medilocate.scheduler;

import com.medilocate.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.medilocate.constants.AppConstants.APPOINTMENT_STORE_LIMIT;

@Component
@RequiredArgsConstructor
public class AppointmentCleanupScheduler {
    // If we want to remove Old Appointment Record like that of 60 days, then it will delete
    private final AppointmentRepository appointmentRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void deleteOldAppointments() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(APPOINTMENT_STORE_LIMIT);
        appointmentRepository.deleteByStartTimeBefore(cutoffDate);
    }
}