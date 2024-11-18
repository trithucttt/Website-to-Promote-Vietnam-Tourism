package com.trithuc.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListTourInCart {
    private Long postTourId;
    private String tourName;
    private String fullNameSupplier;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String tourImageName;
    private Double price;
//    private List<String> locationName;
   private List<String> imageTourUrl;
}
