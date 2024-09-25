package com.trithuc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trithuc.dto.DestinationDto;
import com.trithuc.dto.PostDto;
import com.trithuc.dto.TourDto;
import com.trithuc.model.*;
import com.trithuc.request.AddPostRequest;
import com.trithuc.request.AddTourRequest;
import com.trithuc.request.DestinationRequest;
import com.trithuc.response.MessageResponse;
import com.trithuc.response.PaginationResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TravelContentService {
    List<PostDto> getAllPost();

    ResponseEntity<PaginationResponse> searchAndPaginationPost(String title, int size, int currentPage);

    ResponseEntity<PaginationResponse> manualPagination(String title, int size, int currentPage);

    PostDto getDetailPost(Long postId);

    Post getPostById(Long postId);

    TourDto getDetailTour(Long tourId);

    TourDto DetailTourFolloPost(Long tourId, Long postID);

    List<PostDto> convertListPost(List<Post> posts);

    List<TourDto> convertTour(List<Tour> tours);

    List<DestinationDto> convertDestination(List<Destination> destinations);

    List<TourDto> getTourByUser(Long userId);

    List<TourDto> getTourByTokenUser(String token);

    List<DestinationDto> getDestinationByUser(Long userId);

    Resource getImageTour(Long tourId);

    Resource getImageDestination(Long desId);

    Resource loadImagePost(String imageName);

    public List<PostDto> searchByName(String name, LocalDateTime startTime, LocalDateTime endTime);

    List<PostDto> sortByPrice();

    List<PostDto> sortByTitle();

    List<PostDto> getPostByUser(Long useId);

    List<Tour> listTourByToken(String username);

    List<Destination> listDestinationByToken(String username);

    List<DestinationDto> getTableDestination(String username);

    List<City> getAllCity();


    ResponseEntity<?> findDistrictsByCityId(Long id);

    ResponseEntity<?> findWardsByDistrictId(Long id);

    String createDestination(String name, String address, Long wardId,String description, MultipartFile image, String username);

    MessageResponse createNewTour(String addTourRequest, MultipartFile image) throws JsonProcessingException;

    ResponseEntity<MessageResponse> createNewPost(AddPostRequest addPostRequest, String username);

    MessageResponse deleteTourById(Long tourId);

    TourDto getTourById(Long tourId);

    List<TourDto> getToursByPostId(Long postId);

    MessageResponse deletePostById(Long postId);
}
