package com.medilocate.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_email", columnList = "email", unique = true)
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String hospital;

    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, String> availability = new LinkedHashMap<>();

    private Double latitude;

    private Double longitude;

    @NotBlank(message = "City cannot be empty")
    private String city;

    @Enumerated(EnumType.STRING)
    private DoctorStatus status;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments = new ArrayList<>();

//    @OneToMany(mappedBy = "doctor")
//    private List<Slot> slots = new ArrayList<>();

    private Integer slotSize; // In Minutes

    @Email
    @Column(unique = true)
    private String email;

    @Column(length = 15)
    private String phone;

    @CreatedDate
    private LocalDateTime createdAt;

    @Transient
    private Double distance;

//    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Doctor doctor = (Doctor) o;

        if (!id.equals(doctor.id)) return false;
        return name.equals(doctor.name);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                '}';
    }
}