package com.medilocate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter @Setter
public class DoctorSlotConfiguration {
    // This will be Slots that a Doctor have for daily schedule
    // Many DoctorSlotConfiguration will be related to one Doctor

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Doctor doctor;

    private LocalTime startTime;
    private LocalTime endTime;

    private Integer slotDurationInMinutes; // Optional
}
