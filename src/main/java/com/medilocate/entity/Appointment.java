package com.medilocate.entity;

import com.medilocate.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Doctor doctor;

    @ManyToOne
    private Patient patient;

    @Future(message = "Appointment time must be in the future")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Future(message = "Appointment End time must be in the future")
    @Column(nullable = true)
    private LocalDateTime endTime;

    @Column(length = 1000, nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

}