package com.trithuc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trithuc.config.JWTTokenUtil;
import com.trithuc.constant.TravelErrorConstant;
import com.trithuc.dto.CommentDto;
import com.trithuc.dto.DestinationDto;
import com.trithuc.dto.PostDto;
import com.trithuc.dto.TourDto;
import com.trithuc.exception.TravelException;
import com.trithuc.model.*;
import com.trithuc.repository.*;
import com.trithuc.request.AddPostRequest;
import com.trithuc.request.AddPostTourRequest;
import com.trithuc.request.AddTourRequest;
import com.trithuc.request.TourSchedule;
import com.trithuc.response.MessageResponse;
import com.trithuc.response.Pagination;
import com.trithuc.response.PaginationResponse;
import com.trithuc.service.FileStoreService;
import com.trithuc.service.TravelContentService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelContentServiceImpl implements TravelContentService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private DestinationRepository destinationRepository;
    @Autowired
    private FileStoreService fileStoreService;
    @Autowired
    private JWTTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private WardRepository wardRepository;
    @Autowired
    private ImageRepository imageRepository;

    /*======================================get info===========================================*/
    @Override
    public List<PostDto> getAllPost() {
        List<Post> postList = postRepository.findAll();
        return convertListPost(postList);
    }

    /**
     * Pagination with pageable.
     * */
    @Override
    public ResponseEntity<PaginationResponse> searchAndPaginationPost(String title, int size, int currentPage) {
        if (title == null || title.isEmpty()) {
            title = "%";
        } else {
            title = "%" + title + "%";
        }
        Pageable pageable = PageRequest.of(currentPage - 1, size);
        Page<Post> postPage = postRepository.findByTitleLikeIgnoreCaseAndIsDeleteFalse(pageable, title);
        List<PostDto> postDtos = convertListPost(postPage.getContent());
        Pagination pagination = Pagination.builder()
                .currentPage(currentPage)
                .size(size)
                .totalPage(postPage.getTotalPages())
                .totalResult((int) postPage.getTotalElements())
                .build();
        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setData(postDtos);
        paginationResponse.setPagination(pagination);
        return ResponseEntity.ok(paginationResponse);
    }

    /**
     * Pagination manual.*/
    @Override
    public  ResponseEntity<PaginationResponse> manualPagination(String title, int size, int currentPage){
        if (title == null || title.isEmpty()) {
            title = "%";
        } else {
            title = "%" + title + "%";
        }
        List<Post> posts = postRepository.findAllByTitleLikeIgnoreCaseAndIsDeleteFalse(title);
        int totalResult = posts.size();
        int totalPage = (int) Math.ceil((double) totalResult / size);
        int startIndex = (currentPage -1) * size;
        int endIndex = Math.min(startIndex + size,totalResult);

        List<Post> paginationPosts = posts.subList(startIndex,endIndex);

        List<PostDto> postDtos = convertListPost(paginationPosts);
        Pagination pagination = Pagination.builder()
                .currentPage(currentPage)
                .size(size)
                .totalPage(totalPage)
                .totalResult(totalResult)
                .build();
        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setData(postDtos);
        paginationResponse.setPagination(pagination);
        return ResponseEntity.ok(paginationResponse);

    }

    @Override
    public PostDto getDetailPost(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        return (post != null) ? mapDataPost(post) : null;
    }

    @Override
    public Post getPostById(Long postId) {
        // TODO Auto-generated method stub
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()){
            throw  new  TravelException(TravelErrorConstant.ERROR_CODE_POST_NOT_FOUND);
        }
        return postOptional.get();
    }

    @Override
    public List<PostDto> getPostByUser(Long userId) {
        // TODO Auto-generated method stub
        return convertListPost(postRepository.findPostsByUserId(userId));
    }

    /// ko su dung
    @Override
    public TourDto getDetailTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId).orElse(null);
        return (tour != null) ? conVertDetailTour(tour) : null;
    }

    @Override
    public TourDto DetailTourFolloPost(Long postID, Long tourId) {
        Optional<Tour> tour = tourRepository.findById(tourId);
        return tour.map(this::conVertDetailTourFollowPost).orElse(null);
    }

    private TourDto conVertDetailTourFollowPost(Tour tour) {
        TourDto tourDto = new TourDto();

        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setDescription(tour.getDescription());
        tourDto.setImageTour(tour.getImage_tour());
        tourDto.setPrice(tour.getPrice());
        List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
        tourDto.setDestiationDtoList(destinationDtos);
        tourDto.setQuantityTour(tour.getQuantity());
        tourDto.setEndTime(tour.getEndTimeTour());
        tourDto.setStartTime(tour.getStartTimeTour());
        tourDto.setDiscount(tour.getDiscount());

        List<CommentDto> commentDtos = convertComment(commentRepository.findByTourId(tour.getId()));
        tourDto.setCommentList(commentDtos);
        return tourDto;
    }

    public List<CommentDto> convertComment(List<Comment> comments) {
        return comments.stream().map(comment -> {
            CommentDto commentDto = new CommentDto();
            commentDto.setUserComment(comment.getUser().getFirstname() + " " + comment.getUser().getLastname());
            commentDto.setContent(comment.getContent());
            commentDto.setStartTime(comment.getStart_time());
            commentDto.setId(comment.getId());
            commentDto.setRating(comment.getRating());
            commentDto.setUsernameUserComment(comment.getUser().getUsername());
            List<Image> image = imageRepository.findByCommentId(comment.getId());
            commentDto.setImageComment(image);
            return commentDto;
        }).collect(Collectors.toList());
    }

    private TourDto conVertDetailTour(Tour tour) {
        TourDto tourDto = new TourDto();
        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setDescription(tour.getDescription());
//        tourDto.setQuantityTour(tour.getQuantity());
        tourDto.setImageTour(tour.getImage_tour());
//        tourDto.setDayTour(tour.getDay_tour());
        tourDto.setPrice(tour.getPrice());
//        tourDto.setStartTime(tour.getStart_time());
//        tourDto.setEndTime();
        List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
        tourDto.setDestiationDtoList(destinationDtos);
        return tourDto;
    }

    public PostDto mapDataPost(Post post) {
        PostDto postDto = new PostDto();
        postDto.setPostId(post.getId());
        postDto.setOwnerPostId(post.getUsers().getId());
        postDto.setFullNameUser(post.getUsers().getFirstname() + ' ' + post.getUsers().getLastname());
        postDto.setTitle(post.getTitle());
        postDto.setStart_time(post.getStartTime());
        postDto.setEnd_time(post.getEndTime());
        postDto.setAvatarUser(post.getUsers().getProfileImage());
        List<TourDto> tourDtos = convertTour(tourRepository.findToursByPostId(post.getId()));

        postDto.setTourDtoList(tourDtos);

        List<String> imagePost = tourDtos.stream()
                .map(TourDto::getImageTour).collect(Collectors.toList());
        postDto.setImagePost(imagePost);

        return postDto;
    }


    @Override
    public List<PostDto> convertListPost(List<Post> posts) {
        if (posts == null) {
            return Collections.emptyList(); // or handle null case based on your requirements
        }
        return posts.stream().map(post -> {
            PostDto postDto = new PostDto();
            postDto.setPostId(post.getId());
            postDto.setOwnerPostId(post.getUsers().getId());
            postDto.setFullNameUser(post.getUsers().getFirstname() + ' ' + post.getUsers().getLastname());
            postDto.setTitle(post.getTitle());
            postDto.setStart_time(post.getStartTime());
            postDto.setEnd_time(post.getEndTime());
            List<TourDto> tourDtos = convertTour(tourRepository.findToursByPostId(post.getId()));
//            for (Tour tour : post.getTours()) {
//                TourDto tourDto = getTourDto(tour);
//                tourDtos.add(tourDto);
//            }
            postDto.setTourDtoList(tourDtos);
            postDto.setAvatarUser(post.getUsers().getProfileImage());
            Double avgPriceTour = tourDtos.isEmpty() ? 0 :
                    tourDtos.stream().mapToDouble(TourDto::getPrice).average().orElse(0);
            postDto.setPrice(avgPriceTour);

            List<String> imagePost = tourDtos.stream()
                    .map(TourDto::getImageTour).collect(Collectors.toList());
            postDto.setImagePost(imagePost);

            List<Double> discounts = tourRepository.findDiscountsByPostId(post.getId());
            double avgDiscount = discounts.isEmpty() ? 0 : discounts.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            postDto.setAvgDiscount(avgDiscount);

//            Double averageNumberOfComments = commentRepository.findAverageNumberOfCommentsByPostId(post.getId());
//            postDto.setRateAvg(averageNumberOfComments);

            return postDto;
        }).collect(Collectors.toList());
    }

    private  TourDto getTourDto(Tour tour) {
        TourDto tourDto = new TourDto();
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setPrice(tour.getPrice());
        tourDto.setDiscount(tour.getDiscount());
        tourDto.setStartTime(tour.getStartTimeTour());
        tourDto.setEndTime(tour.getEndTimeTour());
        tourDto.setQuantityTour(tour.getQuantity());
        tourDto.setDescription(tour.getDescription());
        tourDto.setImageTour(tour.getImage_tour());
        List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
        tourDto.setDestiationDtoList(destinationDtos);
        return tourDto;
    }


    @Override
    public List<TourDto> convertTour(List<Tour> tours) {
        return tours.stream().map(this::getTourDto).collect(Collectors.toList());
    }

    @Override
    public List<DestinationDto> convertDestination(List<Destination> destinations) {
        return destinations.stream().map(destination -> {
            DestinationDto destinationDto = new DestinationDto();
            destinationDto.setDesId(destination.getId());
            destinationDto.setDesName(destination.getName());
            destinationDto.setDesImage(destination.getImage_destination());
            destinationDto.setDesAddress(destination.getAddress());
            destinationDto.setDescription(destination.getDescription());
            //  destinationDto.setLocation(destination.getWard().getName() + ", " + destination.getWard().getDistrict().getName() + ", " + destination.getWard().getDistrict().getCity().getName());
            return destinationDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TourDto> getTourByUser(Long userId) {
        List<Tour> tours = tourRepository.findToursByManagerId(userId);
        return convertTour(tours);
    }

    @Override
    public List<TourDto> getTourByTokenUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtTokenUtil.getUsernameFromToken(token);
        Optional<User> user = Optional.ofNullable((userRepository.findByUsername(username)).orElse(null));
        List<Tour> tours = tourRepository.findToursByManagerId(user.get().getId());
        return convertTour(tours);
    }

    @Override
    public List<DestinationDto> getDestinationByUser(Long userId) {
        List<Destination> destinations = destinationRepository.findToursByManagerId(userId);
        return convertDestination(destinations);

    }

    /* ==================load image=================================*/
    @Override
    public Resource getImageTour(Long tourId) {
        String imageName = getTourImageFromDatabase(tourId);
        if (imageName != null) {
            return fileStoreService.loadImage(imageName, "tours");
        } else {
            throw new ResourceNotFoundException("Image not found for tour with id:");
        }
    }

    @Override
    public Resource getImageDestination(Long desId) {
        String imageName = destinationRepository.findImageNameByDestinationId(desId);
        if (imageName != null) {
            return fileStoreService.loadImage(imageName, "destinations");
        } else {
            throw new ResourceNotFoundException("Image not found for tour with id:");
        }
    }

    private String getTourImageFromDatabase(Long tourId) {
        return tourRepository.findImageNameByTourId(tourId);
    }

    @Override
    public Resource loadImagePost(String imageName) {

        return fileStoreService.loadImage(imageName, "tours");
    }

    /*--------------------------------------------search------------------------------------------------------*/
    @Override
    public List<PostDto> searchByName(String name, LocalDateTime startTime, LocalDateTime endTime) {

//        List<TourDto> tourDtos = convertTour(tourRepository.findByTitleContainingIgnoreCase(name));
        if (startTime == null && endTime == null) {
            return convertListPost(postRepository.findByTitleContainingOrTourTitleContaining(name));
        }
        if (name == null && endTime == null) {
            return convertListPost(postRepository.findPostByStartTimeGreaterThanEqual(startTime));
        }
        if (name == null && startTime == null) {
            return convertListPost(postRepository.findPostByEndTimeLessThanEqual(endTime));
        }
        return Collections.emptyList();
    }

    @Override
    public List<PostDto> sortByPrice() {
        return convertListPost(postRepository.findAverageTourPriceOrderByAsc());
    }

    @Override
    public List<PostDto> sortByTitle() {
        return convertListPost(postRepository.findAllByOrderByTitleAsc());
    }

    @Override
    public List<Tour> listTourByToken(String username) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username).orElse(null));
        return tourRepository.findToursByManager(user.get());
    }

    @Override
    public List<Destination> listDestinationByToken(String username) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username).orElse(null));
        return destinationRepository.findDestinationsByManager(user.get());
    }

    @Override
    public List<DestinationDto> getTableDestination(String username) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username).orElse(null));
        List<Destination> destinations = destinationRepository.findDestinationsByManager(user.get());
        return destinations.stream().map(destination -> {
            DestinationDto destinationDto = new DestinationDto();
            destinationDto.setDesId(destination.getId());
            destinationDto.setDesName(destination.getName());
            destinationDto.setDesImage(destination.getImage_destination());
            destinationDto.setDesAddress(destination.getAddress());
            destinationDto.setDescription(destination.getDescription());
            destinationDto.setLocation(destination.getWard().getName() + ", " + destination.getWard().getDistrict().getName() + ", " + destination.getWard().getDistrict().getCity().getName());
            return destinationDto;
        }).collect(Collectors.toList());

    }

    @Override
    public List<City> getAllCity() {
        return cityRepository.findAll();
    }

    @Override
    public ResponseEntity<?> findDistrictsByCityId(Long id) {
        Optional<City> city = cityRepository.findById(id);
        if (city.isPresent()) {
            List<District> districts = districtRepository.findByCityId(id);
            return ResponseEntity.ok(districts);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("City ID not found: " + id);
        }
    }

    @Override
    public ResponseEntity<?> findWardsByDistrictId(Long id) {
        Optional<District> district = districtRepository.findById(id);
        if (district.isPresent()) {
            List<Ward> wards = wardRepository.findByDistrictId(id);
            return ResponseEntity.ok(wards);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("District ID not found " + id);

        }
    }



    @Override
    public String createDestination(String name, String address, Long wardId, String description, MultipartFile image, String username) {
        try {
            Optional<Optional<User>> user = Optional.ofNullable(userRepository.findByUsername(username));
            if (user.isEmpty()) {
                return "User Not Found";
            }
            Destination destination = new Destination();
            destination.setName(name);
            destination.setAddress(address);
            destination.setDescription(description);

            destination.setManager(user.get().get());
            Optional<Ward> ward = wardRepository.findById(wardId);
            if (ward.isEmpty()) {
                return "Ward Not Found";
            }
            destination.setWard(ward.get());

            Long maxId = destinationRepository.getMaxId();
            System.out.println("Get Max Id" + maxId);
            String nameImage = (maxId + 1) + "_" + image.getOriginalFilename();
            destination.setImage_destination(nameImage);
            fileStoreService.saveImage(image, maxId + 1, "destinations");
            destinationRepository.save(destination);
            return "Create Destination Successfully";
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    private MessageResponse setUpResponse(String message, String responseCode,Object data){
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setResponseCode(responseCode);
        messageResponse.setMessage(message);
        messageResponse.setData(data);
        return messageResponse;
    }


    public <T> T convertJsonToObject(String jsonString, Class<T> clazz) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.readValue(jsonString, clazz);
    }

    @Override
    public MessageResponse createNewTour(String addTourRequest, MultipartFile image) throws JsonProcessingException {

            AddTourRequest request = convertJsonToObject(addTourRequest, AddTourRequest.class);
            Optional<User> userOptional = userRepository.findById(request.getManagerId());
            if (userOptional.isEmpty()){
                throw new TravelException(TravelErrorConstant.USER_NOT_FOUND);
            }
            User user = userOptional.get();
            Set<Destination> destinations = request.getDestinationIds().stream()
                    .map(id -> destinationRepository.findById(id).orElseThrow(() -> new IllegalFormatFlagsException("Destination Not Found with id: " + id)))
                    .collect(Collectors.toSet());
            Tour newTour = new Tour();
            Long maxId = tourRepository.getMaxId();
            String nameImage = (maxId + 1) + "_" + image.getOriginalFilename();
            fileStoreService.saveImage(image, maxId + 1, "tours");
            newTour.setImage_tour(nameImage);
            newTour.setManager(user);
            newTour.setPrice(request.getPrice());
            newTour.setTitle(request.getTitle());
            newTour.setDescription(request.getDescription());
            newTour.setDiscount(request.getDiscount());
            newTour.setQuantity(request.getQuantity());
            newTour.setStartTimeTour(request.getStartTime());
            newTour.setEndTimeTour(request.getEndTime());
            newTour.setDestination(destinations);
            newTour.setIsDelete(false);
            tourRepository.save(newTour);
            return setUpResponse("Success", "200", null);


    }

    public List<TourSchedule> generateTourSchedule(LocalDateTime startTimePost, LocalDateTime endTimePost, List<AddPostTourRequest> addPostTourRequests) {
        List<TourSchedule> schedules = new ArrayList<>();
        // khởi tạo ngày bắt đầu và kết thúc cho tour > startPost 1 tháng và < endPost 1 tuần
        LocalDateTime startTimePostPlusMonth = startTimePost.plusMonths(1);
        LocalDateTime endTimePostMinusWeek = endTimePost.minusWeeks(1);
        // lay danh sach cac tour duoc set ngay
        List<Tour> existingTour = tourRepository.findAll();
        // luu cac ngay da co chuyen di
        Set<LocalDateTime> usedDates = new HashSet<>();
        for (AddPostTourRequest request : addPostTourRequests) {
            Long tourId = request.getTourId();
            Integer dayTour = request.getDayTour();

            // tinh ngay ket thuc cho tour
            LocalDateTime startTour = startTimePostPlusMonth.plusDays(1);
            LocalDateTime endTour = startTour.plusDays(dayTour - 1).minusSeconds(1);
            if (endTour.isAfter(endTimePostMinusWeek)) {
                endTour = endTimePostMinusWeek;
            }
            while (isTourOverlapping(existingTour, startTour, endTour) || usedDates.contains(startTour)) {
                startTour = startTour.plusDays(1);
                endTour = endTour.plusDays(dayTour - 1);
            }
            if (endTour.isAfter(endTimePostMinusWeek)) {
                endTour = endTimePostMinusWeek;
            }
            TourSchedule tourSchedule = new TourSchedule(tourId, startTour, endTour);
            schedules.add(tourSchedule);
            usedDates.add(startTour);
        }
        return schedules;
    }

    private boolean isTourOverlapping(List<Tour> existingTour, LocalDateTime startTour, LocalDateTime endTour) {
        for (Tour tour : existingTour) {
            if ((endTour.isEqual(tour.getStartTimeTour()) || startTour.isAfter(tour.getStartTimeTour())) &&
                    (startTour.isEqual(tour.getEndTimeTour()) || startTour.isBefore(tour.getEndTimeTour())) ||
                    (endTour.isEqual(tour.getEndTimeTour()) || endTour.isAfter(tour.getStartTimeTour())) &&
                            (endTour.isEqual(tour.getEndTimeTour()) || endTour.isBefore(tour.getEndTimeTour()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public ResponseEntity<MessageResponse> createNewPost(AddPostRequest addPostRequest, String username) {
        MessageResponse messageResponse = new MessageResponse();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User Not Found");
            return ResponseEntity.ok(messageResponse);
        }
        User user = userOptional.get();
        Post post = new Post();
        post.setTitle(addPostRequest.getTitlePost());
        post.setStartTime(addPostRequest.getStartTimePost());
        post.setEndTime(addPostRequest.getEndTimePost());
        post.setIsDelete(false);
        post.setUsers(user);
        List<Tour> tours = new ArrayList<>();

//        for (AddPostTourRequest request : addPostRequest.getAddPostTourRequests()) {
//            Tour tour = new Tour();
//            postTour.setDiscount(request.getDiscount());
//            postTour.setQuantity(request.getQuantity());
//
//            Optional<Tour> tourOptional = tourRepository.findById(request.getTourId());
//            if (tourOptional.isEmpty()) {
//                messageResponse.setResponseCode("404");
//                messageResponse.setMessage("Tour Not Found");
//                return ResponseEntity.ok(messageResponse);
//            }
//            postTour.setTour(tourOptional.get());
//            LocalDateTime tourStart = addPostRequest.getStartTimePost().plusMonths(1).plusDays(1);
//            LocalDateTime tourEnd = tourStart.plusDays(request.getDayTour() - 1);
//            if (tourEnd.isAfter(addPostRequest.getEndTimePost().minusWeeks(1))) {
//                tourEnd = addPostRequest.getEndTimePost().minusWeeks(1);
//            }
//            while (isTourOverlapping(postTourList, tourStart, tourEnd)){
//                tourStart = tourStart.plusDays(1);
//                tourEnd = tourStart.plusDays(request.getDayTour() - 1);
//                if (tourEnd.isAfter(addPostRequest.getEndTimePost().minusWeeks(1))) {
//                    tourEnd = addPostRequest.getStartTimePost().plusDays(1);
//                }
//            }
//            postTour.setStartTimeTour(tourStart);
//            postTour.setEndTimeTour(tourEnd);
//            postTour.setPost(post);
//            postTourRepository.save(postTour);
//            postTourList.add(postTour);

//        }
////        post.setTours(new HashSet<>(postTourList));
//        postRepository.save(post);
//        messageResponse.setResponseCode("200");
//        messageResponse.setMessage("Post created successfully");
//        return ResponseEntity.ok(messageResponse);
        return null;
    }

    @Override
    public  MessageResponse deleteTourById(Long tourId){
        Optional<Tour> tour = tourRepository.findById(tourId);
        if (tour.isPresent()){
            tour.get().setIsDelete(true);
            tourRepository.save(tour.get());
            return setUpResponse("Delete Tour successfully","200",null);
        }
        throw new TravelException(TravelErrorConstant.DELETE_FAILED);
    }

    @Override
    public TourDto getTourById(Long tourId) {
        Optional<Tour> tour = tourRepository.findById(tourId);
        if (tour.isEmpty()){
            throw new TravelException(TravelErrorConstant.ERROR_CODE_TOUR_NOT_FOUND);
        }
        return getTourDto(tour.get());
    }



    @Override
    public List<TourDto> getToursByPostId(Long postId) {
        Post post = getPostById(postId);
        return tourRepository.findByPostId(postId).stream().map(this::getTourDto
        ).toList();
    }

    @Override
    public MessageResponse deletePostById(Long postId) {

        Optional<Post> post = postRepository.findById(postId);
        if (post.isPresent()){
            post.get().setIsDelete(true);
            postRepository.save(post.get());
            return setUpResponse("Delete Post successfully","200",null);
        }
        throw new TravelException(TravelErrorConstant.DELETE_FAILED);
    }
}
