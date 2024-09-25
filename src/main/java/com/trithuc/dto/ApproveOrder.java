package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveOrder {
    private Long id;
    private String UserOrder;
    private String bankCodeOrder;
    private LocalDateTime orderDate;
    private Long totalTourOrder;
    private String status;
}
