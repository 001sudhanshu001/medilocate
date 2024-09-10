package com.medilocate.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSearchResponse {

    private List<DoctorResponseDTO> doctors;
    private int totalPages;

}
