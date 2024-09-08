package com.medilocate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO : Add a Hospital Field and make changes
    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, String> availability = new LinkedHashMap<>();

    private double latitude;
    private double longitude;

    @NotBlank(message = "City cannot be empty")
    private String city;

    @Enumerated(EnumType.STRING) // For current status, like Occupied, Available
    private DoctorStatus status;

    @JsonIgnore // TODO : use DTO
    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;

    // PostGIS
//    @Column(columnDefinition = "GEOGRAPHY(Point, 4326)") // For Better search
//    private Point location;

    @Transient
    private double distance;
}