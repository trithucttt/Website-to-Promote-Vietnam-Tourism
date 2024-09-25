package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPostTourRequest {
    private Long tourId;
    private Integer quantity;
    private Double discount;
    private Integer dayTour;
}
