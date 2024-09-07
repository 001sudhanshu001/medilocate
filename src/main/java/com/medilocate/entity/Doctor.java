package com.medilocate.entity;

import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    @Column(columnDefinition = "jsonb")
    private String availability; // Doctor's Schedule

    private double latitude;
    private double longitude;

    private String city;

    @Enumerated(EnumType.STRING)
    private DoctorStatus status;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;

    @Transient
    private double distance;
}