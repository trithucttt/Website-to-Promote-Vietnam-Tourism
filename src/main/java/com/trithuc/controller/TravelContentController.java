package com.trithuc.controller;

import com.trithuc.dto.DestinationDto;
import com.trithuc.dto.PostDto;
import com.trithuc.dto.TourDto;
import com.trithuc.model.*;
import com.trithuc.request.AddPostRequest;
import com.trithuc.request.AddPostTourRequest;
import com.trithuc.request.DestinationRequest;
import com.trithuc.response.MessageResponse;
import com.trithuc.response.PaginationResponse;
import com.trithuc.service.FileStoreService;
import com.trithuc.service.TravelContentService;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TravelContentController {

    @Autowired
    private TravelContentService travelContentService;

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    UserService userService;


    @GetMapping("/post/list")
    public List<PostDto> getListPost() {
        return travelContentService.getAllPost();
    }

    @GetMapping("post/search")
    public ResponseEntity<PaginationResponse> searchAndPaginationPost(@RequestParam(value = "title", required = false, defaultValue = "") String title,
                                                                      @RequestParam(value = "size", required = false, defaultValue = "3") int size,
                                                                      @RequestParam(value = "currentPage", required = false, defaultValue = "1") int currentPage) {
        return travelContentService.searchAndPaginationPost(title, size, currentPage);
    }

    @GetMapping("post/detail/{postId}")
    public PostDto detailPost(@PathVariable Long postId) {
        System.out.println(postId);
        return travelContentService.getDetailPost(postId);
    }

    @GetMapping("post/{userId}")
    public List<PostDto> getPostByUser(@PathVariable Long userId) {
//        System.out.println(useId);
        return travelContentService.getPostByUser(userId);
    }

    @GetMapping("/tour/{userId}")
    public List<TourDto> getTourByUser(@PathVariable Long userId) {
        return travelContentService.getTourByUser(userId);
    }

    @GetMapping("/tour")
    public List<TourDto> getTourByTokenUser(@RequestHeader(name = "Authorization") String token) {
        return travelContentService.getTourByTokenUser(token);
    }
//    @GetMapping("tour/detail/{tourId}")
//    public TourDto detailTour(@PathVariable Long tourId){
//        System.out.println(tourId);
//        return travelContentService.getDetailTour(tourId);
//    }

    @GetMapping("tour/detail/{postId}/{tourId}")
    public TourDto detailTourFollowPost(@PathVariable Long postId,
                                        @PathVariable Long tourId) {
        System.out.println(postId);
        System.out.println(tourId);
        return travelContentService.DetailTourFolloPost(postId, tourId);
    }

    @GetMapping("/des/{userId}")
    public List<DestinationDto> getDestinationByUser(@PathVariable Long userId) {
        return travelContentService.getDestinationByUser(userId);
    }

    @GetMapping("tours/{tourId}/image")
    public ResponseEntity<Resource> getTourImage(@PathVariable Long tourId) {
        Resource resource = travelContentService.getImageTour(tourId);
        // You may need to set appropriate headers for the response, like content type
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @GetMapping("destination/{desId}/image")
    public ResponseEntity<Resource> getDestinationImage(@PathVariable Long desId) {
        Resource resource = travelContentService.getImageDestination(desId);
        // You may need to set appropriate headers for the response, like content type
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @GetMapping("post/{imageName}/image")
    public ResponseEntity<Resource> getTourImage(@PathVariable String imageName) {
        Resource resource = travelContentService.loadImagePost(imageName);
        // You may need to set appropriate headers for the response, like content type
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @GetMapping("search/title")
    public List<PostDto> searchByName(@RequestParam(required = false) String name) {
        return travelContentService.searchByName(name, null, null);
    }

    @GetMapping("search/timeStart")
    public List<PostDto> searchByStartTime(@RequestParam(required = false) LocalDateTime startTime) {
        return travelContentService.searchByName(null, startTime, null);
    }

    @GetMapping("search/timeEnd")
    public List<PostDto> searchByEndTime(@RequestParam(required = false) LocalDateTime endTime) {
        return travelContentService.searchByName(null, null, endTime);
    }

    @GetMapping("sort/title")
    public List<PostDto> sortByTitle() {
        return travelContentService.sortByTitle();
    }

    @GetMapping("sort/price")
    public List<PostDto> sortByPrice() {
        return travelContentService.sortByPrice();
    }

    @GetMapping("/images")
    public ResponseEntity<List<String>> getAllImageDestination(@RequestParam(required = false, defaultValue = "destinations") String type) {
        List<String> fileNames = fileStoreService.getAllImageNames(type);
        return ResponseEntity.ok().body(fileNames);
    }


    @GetMapping("/business/tours")
    public List<Tour> getToursByToken(@RequestHeader(name = "Authorization") String token) {
        String username = userService.Authentication(token);
        return travelContentService.listTourByToken(username);
    }

    @GetMapping("/business/destination")
    public List<Destination> getDestinationByToken(@RequestHeader(name = "Authorization") String token) {
        String username = userService.Authentication(token);
        return travelContentService.listDestinationByToken(username);
    }

    @GetMapping("/business/table/destination")
    public List<DestinationDto> getTableDestination(@RequestHeader(name = "Authorization") String token) {
        String username = userService.Authentication(token);
        return travelContentService.getTableDestination(username);
    }

    @GetMapping("/business/city")
    public List<City> getAllCity() {
        return travelContentService.getAllCity();
    }

    @GetMapping("/business/district")
    public ResponseEntity<?> findDistrictsByCityId(@RequestParam Long id) {
        return travelContentService.findDistrictsByCityId(id);
    }

    @GetMapping("/business/ward")
    public ResponseEntity<?> findWardByDistrictId(@RequestParam Long id) {
        return travelContentService.findWardsByDistrictId(id);
    }

    @PostMapping("business/destination/save")
    public String createDestination(@RequestParam("destinationName") String destinationName,
                                    @RequestParam("address") String address,
                                    @RequestParam("wardId") Long wardId,
                                    @RequestParam("description")String description,
                                    @RequestParam("image") MultipartFile image,
                                    @RequestHeader(name = "Authorization")
                                    String token) {
        String username = userService.Authentication(token);
        return travelContentService.createDestination(destinationName,address,wardId,description,image,username);
    }

    @PostMapping("business/tour/save")
    public ResponseEntity<MessageResponse> createTour(@RequestParam("titleTour") String titleTour,
                                                      @RequestParam("price") Double price,
                                                      @RequestParam("destinationId") List<Long> destinationId,
                                                      @RequestParam("description")String description,
                                                      @RequestParam("image") MultipartFile image,
                                                      @RequestHeader(name = "Authorization")
                                    String token) {
        String username = userService.Authentication(token);
        return travelContentService.createNewTour(titleTour,price,description,destinationId,image,username);
    }

    @PostMapping("business/post/save")
    public ResponseEntity<MessageResponse> createNewPost(@RequestHeader(name = "Authorization")String token, @RequestBody AddPostRequest addPostRequest){
        return travelContentService.createNewPost(addPostRequest,userService.Authentication(token));
    }

}
