package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueStatisticsDTO {
    private String tourName;
    private Long bookingsCount;
    private Double totalRevenue;
}
