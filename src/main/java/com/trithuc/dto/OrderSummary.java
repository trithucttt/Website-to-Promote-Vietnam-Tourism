package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummary {
    private List<ItemSummary> items;
    private Double totalAmount;
    private int totalQuantity;
}
