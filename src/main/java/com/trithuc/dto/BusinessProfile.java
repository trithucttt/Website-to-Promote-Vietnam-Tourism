package com.trithuc.dto;

import com.trithuc.model.Destination;
import com.trithuc.model.Post;
import com.trithuc.model.Tour;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile extends ProfileDto {
        private List<TourDto> toursDto;
        private List<DestinationDto> destinationsDto;
        private List<PostDto> postsDto;
}
