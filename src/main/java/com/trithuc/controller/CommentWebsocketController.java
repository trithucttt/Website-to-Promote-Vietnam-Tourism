package com.trithuc.controller;

import com.trithuc.dto.CommentDto;
import com.trithuc.request.DeleteCommentRealTimeRequest;
import com.trithuc.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CommentWebsocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CommentService commentService;

    @MessageMapping("/comment.createComment")
//    @SendTo("/topic/comment/{postId}")
    public CommentDto  createComment(Long commentId){
        System.out.println("createComment");
        CommentDto commentDto = commentService.getCommentAndConvertToDTO(commentId);
        commentDto.setType("ADD");
        System.out.println("createComment  " +commentDto.getPostId());
        messagingTemplate.convertAndSend("/topic/comment/" + commentDto.getPostId(),commentDto);

        return commentDto;
    }


    @MessageMapping("/comment.editComment")
//    @SendTo("/topic/comment/{postId}")
    public CommentDto  editComment(Long commentId){
        System.out.println("editComment");
        CommentDto commentDto = commentService.getCommentAndConvertToDTO(commentId);
        commentDto.setType("EDIT");
        System.out.println("editComment  " +commentDto.getPostId());
        System.out.println("editComment  " +commentDto.getType());
        messagingTemplate.convertAndSend("/topic/comment/" + commentDto.getPostId(),commentDto);

        return commentDto;
    }


    @MessageMapping("/comment.deleteComment")
//    @SendTo("/topic/comment/{postId}")
    public CommentDto  deleteComment(DeleteCommentRealTimeRequest request){
        System.out.println("deleteComment");
        CommentDto commentDto = new CommentDto();
        commentDto.setType("DELETE");
        commentDto.setPostId(request.getPostId());
        commentDto.setId(request.getCommentId());
        System.out.println("DELETE");
        messagingTemplate.convertAndSend("/topic/comment/" + request.getPostId(),commentDto);

        return commentDto;
    }

}
