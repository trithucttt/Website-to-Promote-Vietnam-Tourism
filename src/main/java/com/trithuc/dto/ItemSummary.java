package com.trithuc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSummary {
    private String itemName;
    private int quantity;
    private Double price;
    private List<DestinationSummary> destinations;
}
