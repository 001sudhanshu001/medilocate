package com.medilocate.entity;

import com.medilocate.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @OneToOne // Doctor's Slot
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @ManyToOne
    @JoinColumn(name = "booked_by_user_having_id", nullable = false)
    private User bookedBY;

//    @Future(message = "Appointment time must be in the future")
//    @Column(nullable = false)
//    private LocalDateTime startTime;
//
//    @Future(message = "Appointment End time must be in the future")
//    @Column(nullable = true)
//    private LocalDateTime endTime;

    @Column(length = 1000, nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime canceledAt;

}