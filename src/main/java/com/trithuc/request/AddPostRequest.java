package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPostRequest {
    private String titlePost;
    private LocalDateTime endDay;
//    private List<AddPostTourRequest> addPostTourRequests;
    private List<Long> tourId;
}
