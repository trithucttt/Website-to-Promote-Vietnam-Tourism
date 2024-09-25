package com.trithuc.dto;

import com.trithuc.model.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String userComment;
    private String content;
    private LocalDateTime startTime;
    private Short rating;
    private String usernameUserComment;
    private List<Image> imageComment;
}
