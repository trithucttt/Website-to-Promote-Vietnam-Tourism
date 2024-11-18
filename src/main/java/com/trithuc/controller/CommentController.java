package com.trithuc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trithuc.dto.CommentDto;
import com.trithuc.model.Comment;
import com.trithuc.model.Image;
import com.trithuc.request.CommentRequest;
import com.trithuc.request.ReplyRequest;
import com.trithuc.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.apache.tomcat.util.http.fileupload.FileUploadBase.MULTIPART_FORM_DATA;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private SimpMessageSendingOperations messageSendingOperations;

    @PostMapping(value ="create" ,consumes = MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createComment(@RequestPart(value = "newComment") String newComment,
                                           @RequestPart(value = "images", required = false) List<MultipartFile> images) throws JsonProcessingException {

        return ResponseEntity.ok(commentService.createComment(newComment, images));
//       if(comment != null ){
//           messageSendingOperations.convertAndSend("/topic/comments" + comment.getPostTour().getId(),comment);
//       }
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add comment");
    }

    @PostMapping("/reply")
    public ResponseEntity<?> addReply(@RequestBody ReplyRequest replyRequest, @RequestHeader(name = "Authorization") String token) {
        Comment reply = commentService.addReplyToComment(token, replyRequest.getCommentId(), replyRequest.getContent());
        if (reply != null) {
            // Gửi thông tin reply mới tới tất cả clients đang lắng nghe
            messageSendingOperations.convertAndSend("/topic/comments/" + reply.getParent().getId() + "/replies", reply);
            return ResponseEntity.ok("Reply added successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add reply");
    }

    @GetMapping("list/{postId}")
    public List<CommentDto> getListComment(@PathVariable Long postId) {
        return commentService.getListComment(postId);
    }

    @GetMapping("image/{commentId}")
    public List<Image> getImageByComment(@PathVariable Long commentId) {
        return commentService.getImageByComment(commentId);
    }

    @DeleteMapping("delete/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                @RequestHeader(name = "Authorization") String token) {
        return commentService.deleteComment(token, commentId);
    }

    @PutMapping(value = "edit/{commentId}", consumes = MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON_VALUE)
    public String editComment(@PathVariable(value = "commentId") Long commentId,
                              @RequestPart("editCommentRequest") String editCommentRequest,
                              @RequestPart(value = "editImage", required = false) List<MultipartFile> editImage) throws JsonProcessingException {
        return commentService.editComment(commentId, editCommentRequest, editImage);
    }
}
