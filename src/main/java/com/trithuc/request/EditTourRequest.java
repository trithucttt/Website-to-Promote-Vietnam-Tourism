package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditTourRequest {
    private Long tourId;
    private String editTitle;
    private String editDescription;
    private Double editPrice;
    private List<Long> editDestination;
    private Integer editQuantity;
    private Double editDiscount;
    private LocalDateTime editStartTime;
    private LocalDateTime editEndTime;
    List<Long> updateImageIds;

}
