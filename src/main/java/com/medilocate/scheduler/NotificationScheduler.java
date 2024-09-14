package com.medilocate.scheduler;

import com.medilocate.entity.Appointment;
import com.medilocate.entity.enums.AppointmentStatus;
import com.medilocate.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final AppointmentRepository appointmentRepository;
    private final JavaMailSender mailSender;

    //    @Scheduled(cron = "0 */15 * * * ?") // Runs every 15 minutes
    @Scheduled(fixedDelay = 900000)
    public void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<Appointment> upcomingAppointments = appointmentRepository.findAppointmentsForNotification(
                now,
                oneHourLater,
                AppointmentStatus.BOOKED
        );

        List<Long> appointmentIds = new ArrayList<>();

        for (Appointment appointment : upcomingAppointments) {
            if (!appointment.getNotificationSent()) {
                sendEmailNotification(appointment);
                appointmentIds.add(appointment.getId());
            }

            // TODO : Just for demonstration
            if(appointment.getBookedBY().getEmail().equals("user@gmail.com")) {
                sendEmailNotification(appointment, "sudhanshuarya305@gmail.com");
            }
        }

        if (!appointmentIds.isEmpty()) {
            appointmentRepository.updateNotificationSentStatus(appointmentIds);
        }
    }

    private void sendEmailNotification(Appointment appointment) {
        Double doctorLatitude = appointment.getDoctor().getLatitude();
        Double doctorLongitude = appointment.getDoctor().getLongitude();
        String locationUrl = createGoogleMapsLocationUrl(doctorLatitude, doctorLongitude);

        String subject = "Upcoming Appointment Reminder";
        String body = String.format(
                "Dear %s, \n\nThis is a reminder for your upcoming appointment with Dr. %s on %s.\n\nLocation: %s\n\nView on Google Maps: %s\n\nBest regards,\nMediLocate Team",
                appointment.getBookedBY().getName(),
                appointment.getDoctor().getName(),
                appointment.getStartTime().toString(),
                "Doctor's Clinic",
                locationUrl
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(appointment.getBookedBY().getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private String createGoogleMapsLocationUrl(Double latitude, Double longitude) {
        if(latitude != null && longitude != null) {
            return String.format(
                    "https://www.google.com/maps?q=%f,%f",
                    latitude, longitude
            );
        }
        return null;
    }

    /// TODO : Just for demonstration
    private void sendEmailNotification(Appointment appointment, String email) {
        Double doctorLatitude = appointment.getDoctor().getLatitude();
        Double doctorLongitude = appointment.getDoctor().getLongitude();
        String locationUrl = createGoogleMapsLocationUrl(doctorLatitude, doctorLongitude);

        String subject = "Upcoming Appointment Reminder";
        String body = String.format(
                "Dear %s, \n\nThis is a reminder for your upcoming appointment with Dr. %s on %s.\n\nLocation: %s\n\nView on Google Maps: %s\n\nBest regards,\nMediLocate Team",
                appointment.getBookedBY().getName(),
                appointment.getDoctor().getName(),
                appointment.getStartTime().toString(),
                "Doctor's Clinic",
                locationUrl
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(appointment.getBookedBY().getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
