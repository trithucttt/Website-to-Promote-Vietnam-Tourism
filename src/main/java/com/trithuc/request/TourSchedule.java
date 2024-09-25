package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourSchedule {
    private Long tourId;
    private LocalDateTime startTimeTour;
    private LocalDateTime endTimeTour;
}
