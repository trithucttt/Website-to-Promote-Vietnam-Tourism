package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourBookingStatsDTO {
    private Long id;
    private String title;
    private Double price;
    private Integer quantity;
    private Long bookedQuantity;
}
