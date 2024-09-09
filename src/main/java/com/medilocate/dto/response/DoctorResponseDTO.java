package com.medilocate.dto.response;

import com.medilocate.entity.enums.DoctorStatus;
import com.medilocate.entity.enums.Specialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {
    private Long id;

    private String name;

    private String hospital;

    private Specialty specialty;

    private Map<String, String> availability = new LinkedHashMap<>();

    private Double latitude;

    private Double longitude;

    private String city;

    private DoctorStatus status;

    private Double distance;

    private String email;
}
