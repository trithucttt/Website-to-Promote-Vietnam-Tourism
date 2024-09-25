package com.trithuc.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoTourItems {
    private Long itemId;
    private Double price;
    private Integer quantityItem;
    private String itemName;
    private LocalDateTime startTimeItem;
    private LocalDateTime endTimeItem;
    private Double discountItem;
    private String status;
}
