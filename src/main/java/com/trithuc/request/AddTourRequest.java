package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddTourRequest {
    private String title;
    private String description;
    private Double price;
    private Double discount;
    private LocalDateTime endTime;
    private LocalDateTime startTime;
    private Integer quantity;
    private List<Long> destinationIds;
    private Long managerId;

}
