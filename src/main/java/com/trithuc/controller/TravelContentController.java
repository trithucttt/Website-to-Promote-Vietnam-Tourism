package com.trithuc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trithuc.dto.DestinationDto;
import com.trithuc.dto.EditTourDTO;
import com.trithuc.dto.PostDto;
import com.trithuc.dto.TourDto;
import com.trithuc.model.*;
import com.trithuc.request.AddPostRequest;
import com.trithuc.request.CreatePostNonSale;
import com.trithuc.request.EditPostRequest;
import com.trithuc.response.MessageResponse;
import com.trithuc.response.PaginationResponse;
import com.trithuc.service.FileStoreService;
import com.trithuc.service.TravelContentService;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.tomcat.util.http.fileupload.FileUploadBase.MULTIPART_FORM_DATA;

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

//    @GetMapping("post/search")
//    public ResponseEntity<PaginationResponse> searchAndPaginationPost(@RequestParam(value = "title", required = false, defaultValue = "") String title,
//                                                                      @RequestParam(value = "size", required = false, defaultValue = "3") int size,
//                                                                      @RequestParam(value = "currentPage", required = false, defaultValue = "1") int currentPage) {
//        return travelContentService.searchAndPaginationPost(title, size, currentPage);
//    }

    @GetMapping("post/search")
    public ResponseEntity<PaginationResponse> searchAndPaginationPost(@RequestParam(value = "title", required = false, defaultValue = "") String title,
                                                                      @RequestParam(value = "size", required = false, defaultValue = "3") int size,
                                                                      @RequestParam(value = "currentPage", required = false, defaultValue = "1") int currentPage) {
        return travelContentService.manualPagination(title, size, currentPage);
    }

    @GetMapping("post/filter")
    public ResponseEntity<PaginationResponse> manualPagination(
            @RequestParam(value = "title", required = false, defaultValue = "") String title,
            @RequestParam(value = "startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "quantityStart", required = false) Integer quantityStart,
            @RequestParam(value = "quantityEnd", required = false) Integer quantityEnd,
            @RequestParam(value = "priceStart", required = false) Double priceStart,
            @RequestParam(value = "priceEnd", required = false) Double priceEnd,
            @RequestParam(value = "discountStart", required = false) Double discountStart,
            @RequestParam(value = "discountEnd", required = false) Double discountEnd,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "regionName", required = false) String regionName,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "currentPage", defaultValue = "1") int currentPage) {

        return travelContentService.SearchAndFilterAndPagination(title, startTime,quantityStart, quantityEnd, priceStart, priceEnd, discountStart, discountEnd, cityId, regionName, size, currentPage);
    }

    @GetMapping("post/detail/{postId}")
    public PostDto detailPost(@PathVariable Long postId) {
        System.out.println(postId);
        return travelContentService.getDetailPost(postId);
    }

    @GetMapping("post/get/{postId}")
    public ResponseEntity<Post> getDetailPost(@PathVariable Long postId) {
        System.out.println(postId);
        return ResponseEntity.ok(travelContentService.getPostById(postId));
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

    @PutMapping("/business/tour/delete/{tourId}")
    public ResponseEntity<MessageResponse> deleteTourById(@PathVariable Long tourId){
        return ResponseEntity.ok(travelContentService.deleteTourById(tourId));
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
    public List<TourDto> getToursByToken(@RequestHeader(name = "Authorization") String token) {
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

    @PostMapping(value = "business/destination/edit", consumes = MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON_VALUE)
    public MessageResponse editDestination( @RequestPart("updateDestination")String data,
                                    @RequestPart(value = "image",required = false) MultipartFile image,
                                    @RequestHeader(name = "Authorization")
                                    String token) {
        String username = userService.Authentication(token);
        return travelContentService.updateDestination(data,image,username);
    }

    @DeleteMapping("/destination/{desId}")
    public ResponseEntity<MessageResponse> deleteDestination(@PathVariable("desId") Long id) {
        return ResponseEntity.ok(travelContentService.deleteDestinationById(id));
    }

    @PostMapping(value = "business/tour/save", consumes = MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> createTour(@RequestPart(value = "addTourRequest") String addTourRequest,
                                                      @RequestPart(value = "images") List<MultipartFile> images) throws JsonProcessingException {

        return ResponseEntity.ok(travelContentService.createNewTour(addTourRequest,images));
    }

    @PostMapping("business/post/save")
    public ResponseEntity<MessageResponse> createNewPost(@RequestHeader(name = "Authorization")String token, @RequestBody AddPostRequest addPostRequest){
        return travelContentService.createNewPost(addPostRequest,userService.Authentication(token));
    }

    @GetMapping("/business/tour/{tourId}")
    public EditTourDTO getTourById(@PathVariable Long tourId) {
        return travelContentService.getTourById(tourId);
    }

    @GetMapping("/business/post/getTour/{postId}")
    public List<TourDto> getToursByPostId(@PathVariable Long postId){
        return travelContentService.getToursByPostId(postId);
    }

    @PutMapping("/business/post/delete/{postId}")
    public ResponseEntity<MessageResponse> deletePostById(@PathVariable Long postId){
        return ResponseEntity.ok(travelContentService.deletePostById(postId));
    }

    @PostMapping("/business/post/edit")
    public ResponseEntity<MessageResponse> editBusinessPost(@RequestBody EditPostRequest updatePost){
        return ResponseEntity.ok(travelContentService.editBusinessPost(updatePost));
    }

    @PutMapping("/business/tour/edit")
    public ResponseEntity<MessageResponse> editBusinessTour(@RequestPart String editTourRequest,@RequestPart(required = false) List<MultipartFile> editImages) throws JsonProcessingException {
        return ResponseEntity.ok(travelContentService.editBusinessTour(editTourRequest,editImages));
    }

    @PostMapping("user/new/post")
    public ResponseEntity<MessageResponse> createPostNonSale(@RequestPart String data,@RequestPart(required = false) List<MultipartFile> images) throws JsonProcessingException {
        return ResponseEntity.ok(travelContentService.createPostNonSale(data, images));
    }

    @PostMapping("user/edit/post")
    public ResponseEntity<MessageResponse> editPostNonSale(@RequestPart String editPostRequest,@RequestPart(required = false) List<MultipartFile> images) throws JsonProcessingException {
        return ResponseEntity.ok(travelContentService.EditPostNonSale(editPostRequest, images));
    }

}
