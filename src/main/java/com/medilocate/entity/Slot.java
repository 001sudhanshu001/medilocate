package com.medilocate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medilocate.entity.enums.SlotStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Future(message = "Start time must be in the future")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Future(message = "End time must be in the future")
    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    // TODO : Use DTO
    @JsonIgnore
    boolean isDeleted;

}
