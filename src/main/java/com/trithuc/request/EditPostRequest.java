package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditPostRequest {
    private Long postId;
    private String title;
    private List<Long> tours;

}
