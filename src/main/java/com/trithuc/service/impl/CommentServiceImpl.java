package com.trithuc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trithuc.dto.CommentDto;
import com.trithuc.model.*;
import com.trithuc.repository.*;
import com.trithuc.request.CommentRequest;
import com.trithuc.request.EditCommentRequest;
import com.trithuc.response.MessageResponse;
import com.trithuc.service.CommentService;
import com.trithuc.service.FileStoreService;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private SimpMessageSendingOperations messageTemplate;
    @Autowired
    private FileStoreService fileStoreService;
    @Autowired
    private ImageRepository imageRepository;

    @Override
    @Transactional
    public MessageResponse createComment(String addCommentRequest, List<MultipartFile> images) throws JsonProcessingException {
        MessageResponse messageResponse = new MessageResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        CommentRequest request = objectMapper.readValue(addCommentRequest, CommentRequest.class);

        Optional<User> currentUser = userRepository.findByUsername(request.getUsername());
        Optional<Post> postOptional = postRepository.findById(request.getPostId());
        if (currentUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        if (postOptional.isPresent()) {
            Comment comment = new Comment();
            comment.setContent(request.getContent());
            comment.setPost(postOptional.get());
            comment.setStart_time(LocalDateTime.now());
            comment.setUser(currentUser.get());
            if (request.isAnonymous()){
                comment.setState(State.ANONYMOUS);
            }else {
                comment.setState(State.PUBLIC);
            }
            
            Comment commentSave = commentRepository.save(comment);
//            System.out.println("get image from comment" + images);
            if (images != null) {
                List<Image> imageList = new ArrayList<>();
                images.forEach(image -> {
                    Image newCommentImage = new Image();
                    newCommentImage.setEntityType("comment");
                    newCommentImage.setComment(commentSave);
                    try {
                        newCommentImage.setImageUrl(fileStoreService.saveImageCloudinary(image));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    imageList.add(newCommentImage);
                });
                imageRepository.saveAll(imageList);
            }

            messageResponse.setMessage("Xong");
            messageResponse.setResponseCode("200");
            return messageResponse;
        } else {
            messageResponse.setMessage("Lỗi");
            messageResponse.setResponseCode("400");
            return messageResponse;
        }
    }

    public List<CommentDto> convertComment(List<Comment> comments) {
        return comments.stream().map(comment -> {
            CommentDto commentDto = new CommentDto();
            commentDto.setUserComment(comment.getUser().getFirstname() + " " + comment.getUser().getLastname());
            commentDto.setContent(comment.getContent());
            commentDto.setStartTime(comment.getStart_time());
            commentDto.setId(comment.getId());
//            commentDto.setRating(comment.getRating());
            commentDto.setUsernameUserComment(comment.getUser().getUsername());
            List<Image> image = imageRepository.findByCommentId(comment.getId());
            commentDto.setImageComment(image);
            return commentDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getListComment(Long postId) {

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new RuntimeException("Post id or tour id is incorrect");
        }
        List<Comment> comments = commentRepository.findByPostId(post.get().getId());
        List<CommentDto> commentsDTO = new ArrayList<>();
        List<Comment> parenComments = comments.stream().filter(comment -> comment.getParent() == null).toList();
        for (Comment parenComment : parenComments){
            CommentDto commentDto = convertToDTO(parenComment);
            commentDto.setReplies(getRepliesRecursive(parenComment, comments));
            commentsDTO.add(commentDto);
        }
        return commentsDTO;
    }

    private CommentDto convertToDTO(Comment comment){
        List<Image> images = imageRepository.findByCommentId(comment.getId());
        return new CommentDto(
                comment.getId(),
                comment.getUser().getLastname()+ " " + comment.getUser().getFirstname(),
                comment.getUser().getProfileImage(),
                comment.getContent(),
                comment.getStart_time(),
//                comment.getRating(),
                comment.getUser().getUsername(),
                new ArrayList<>(images),
                new ArrayList<>(),
                getStateComment(comment.getState())
        );
    }

    private boolean getStateComment(State state) {
        return state == State.ANONYMOUS;
    }

    private List<CommentDto> getRepliesRecursive(Comment parentComment, List<Comment> allComments){
        List<CommentDto> replies = new ArrayList<>();
        for (Comment reply : allComments){
            if (reply.getParent() != null && reply.getParent().getId().equals(parentComment.getId())){
                CommentDto replyDTO = convertToDTO(reply);
                // su dung de quy de tim comment phan cap
                replyDTO.setReplies(getRepliesRecursive(reply, allComments));
                replies.add(replyDTO);
            }

        }
        return replies;
    }

    @Override
    public List<Image> getImageByComment(Long commentId) {
        List<Image> imageList = imageRepository.findByCommentId(commentId);
        if (imageList.isEmpty()) {
            return null;
        }
        return imageList;
    }

    @Override
    @Transactional
    public Comment addReplyToComment(String token, Long commentId, String content) {

        String username = userService.Authentication(token);
        Optional<User> currentUser = userRepository.findByUsername(username);
        if (currentUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        Comment parentComment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        Comment repComment = new Comment();
        repComment.setUser(currentUser.get());
        repComment.setContent(content);
        repComment.setStart_time(LocalDateTime.now());
        repComment.setParent(parentComment);
        repComment.setPost(parentComment.getPost());
        parentComment.getReplies().add(repComment);
        return commentRepository.save(repComment);

    }

    @Override
    public ResponseEntity<String> deleteComment(String token, Long commentId) {
        String username = userService.Authentication(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        }
        Comment comment = commentOptional.get();

        if (!comment.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to delete this comment");
        }

        List<Image> imageList = imageRepository.findByCommentId(commentId);
        imageRepository.deleteAll(imageList);

        commentRepository.deleteById(commentId);
        return ResponseEntity.ok("200");
    }


    @Override
    public void sendCommentUpdate(Comment comment) {
        messageTemplate.convertAndSend("/topic/comments" + comment.getPost().getId(), comment);
    }

    @Override
    public void sendReplyUpdate(Comment repCm) {
        messageTemplate.convertAndSend("/topic/comments" + repCm.getParent().getId() + "/replies", repCm);
    }

    @Override
    public String editComment(Long commentId, String editCommentRequest, List<MultipartFile> editImage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        EditCommentRequest request = objectMapper.readValue(editCommentRequest, EditCommentRequest.class);
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return "404";
        }
        Comment comment = commentOptional.get();
        List<Image> imageList = imageRepository.findByCommentId(commentId);
//        System.out.println("get image from comment imageList" + imageList);
        List<String> updateImageUrl = request.getUpdateImageUrl();
        List<Image> deleteImageUrl = imageList.stream()
                .filter(image -> !updateImageUrl.contains(image.getImageUrl()))
                .toList();

        if (!deleteImageUrl.isEmpty()) {
            List<Long> deleteImageIds = deleteImageUrl.stream()
                    .map(Image::getId)
                    .toList();
            imageRepository.deleteAllById(deleteImageIds);
        }

        comment.setContent(request.getEditContent());
        Comment saveComment = commentRepository.save(comment);
//        System.out.println("get image from comment" + editImage);
        if (editImage != null) {
            List<Image> newImageList = new ArrayList<>();
            editImage.forEach(image -> {
                Image newCommentImage = new Image();
                newCommentImage.setEntityType("comment");
                newCommentImage.setComment(saveComment);

                try {
                    newCommentImage.setImageUrl(fileStoreService.saveImageCloudinary(image));
//                    System.out.println("get image from comment" + fileStoreService.saveImageCloudinary(image));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                newImageList.add(newCommentImage);
            });
            imageRepository.saveAll(newImageList);
            return "200";
        }else{
            return "200";
        }

    }
}
