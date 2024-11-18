package com.trithuc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trithuc.model.Comment;
import com.trithuc.model.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TourDto implements Serializable {
    private  Long tour_id;
    private String titleTour;
    private String companyTour;
    private String companyAvatar;
    private Integer ratingDto;
    private String description;
    private Double price;
    private Double discount;

    private LocalDateTime startTime;
    private  LocalDateTime endTime;
    private  Integer quantityTour;

    private  List<String> imageTour;
    private List<DestinationDto> destiationDtoList;
}
