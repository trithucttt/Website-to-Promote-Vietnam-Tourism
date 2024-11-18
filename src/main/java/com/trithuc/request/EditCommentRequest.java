package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditCommentRequest {

    private String editContent;
    private List<String> updateImageUrl;
}
