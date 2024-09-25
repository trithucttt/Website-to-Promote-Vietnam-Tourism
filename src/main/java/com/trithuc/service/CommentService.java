package com.trithuc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trithuc.dto.CommentDto;
import com.trithuc.model.Comment;
import com.trithuc.model.Image;
import com.trithuc.request.CommentRequest;
import com.trithuc.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentService {


    @Transactional
    MessageResponse createComment(String addCommentRequest, List<MultipartFile> images) throws JsonProcessingException;


    List<CommentDto> getListComment(Long postId, Long tourId);

    List<Image> getImageByComment(Long commentId);

    @Transactional
    Comment addReplyToComment(String token, Long commentId, String content);

    ResponseEntity<String> deleteComment(String token, Long commentId);

    void sendCommentUpdate(Comment comment);

    void sendReplyUpdate(Comment repCm);

    String editComment(Long commentId, String editCommentRequest, List<MultipartFile> editImage) throws JsonProcessingException;
}
