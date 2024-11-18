package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourBookingItemDTO {
    private Long bookingId;
    private String tourTitle;
    private String customerName;
    private Integer quantity;
    private Double price;
    private String status;
}
