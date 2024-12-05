package com.trithuc.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteCommentRealTimeRequest {

    private Long postId;
    private Long commentId;
}
