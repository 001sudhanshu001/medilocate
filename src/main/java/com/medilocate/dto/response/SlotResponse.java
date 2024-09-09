package com.medilocate.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {
    Long slotId;

    Long doctorId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
