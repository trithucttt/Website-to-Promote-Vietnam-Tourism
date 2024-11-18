package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditPostNonSaleRequest {
    private Long postId;
    private List<Long> oldImageIds;
    private String title;
}
