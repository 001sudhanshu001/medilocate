package com.medilocate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // TODO: Will delete it After Testing
    @Column(nullable = true) // This field will be added in Doctor Entity
    private Integer slotDurationInMinutes; // Optional
}
