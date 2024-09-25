package com.trithuc.dto;

import com.trithuc.model.Tour;
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
public class PostDto implements Serializable {
    private  Long postId;
    private String title;
    private String fullNameUser;
    private Long ownerPostId;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private Double price;
    List<TourDto> tourDtoList;
    private List<String> imagePost;
    private Double rateAvg;
    private Double avgDiscount;
    private String avatarUser;
//    private Boolean isDelete;
}
