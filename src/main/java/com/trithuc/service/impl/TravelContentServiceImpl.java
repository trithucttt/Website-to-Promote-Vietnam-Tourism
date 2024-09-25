package com.trithuc.service.impl;

import com.trithuc.config.JWTTokenUtil;
import com.trithuc.dto.CommentDto;
import com.trithuc.dto.DestinationDto;
import com.trithuc.dto.PostDto;
import com.trithuc.dto.TourDto;
import com.trithuc.model.*;
import com.trithuc.repository.*;
import com.trithuc.request.AddPostRequest;
import com.trithuc.request.AddPostTourRequest;
import com.trithuc.request.TourSchedule;
import com.trithuc.response.MessageResponse;
import com.trithuc.response.Pagination;
import com.trithuc.response.PaginationResponse;
import com.trithuc.service.FileStoreService;
import com.trithuc.service.TravelContentService;
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
    private PostTourRepository postTourRepository;
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


    @Override
    public PostDto getDetailPost(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        return (post != null) ? mapDataPost(post) : null;
    }

    @Override
    public Post getPostById(Long postId) {
        // TODO Auto-generated method stub
        return postRepository.findById(postId).orElse(null);
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
        PostTour postTour = postTourRepository.findByPostIdAndTourId(postID, tourId).orElse(null);
        return (postTour != null) ? conVertDetailTourFollowPost(postTour) : null;
    }

    private TourDto conVertDetailTourFollowPost(PostTour postTour) {
        TourDto tourDto = new TourDto();
        Tour tour = postTour.getTour();

        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setDescription(tour.getDescription());
        tourDto.setImageTour(tour.getImage_tour());
        tourDto.setPrice(tour.getPrice());
        List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
        tourDto.setDestiationDtoList(destinationDtos);
        tourDto.setQuantityTour(postTour.getQuantity());
        tourDto.setEndTime(postTour.getEndTimeTour());
        tourDto.setStartTime(postTour.getStartTimeTour());
        tourDto.setDiscount(postTour.getDiscount());

        List<CommentDto> commentDtos = convertComment(commentRepository.findByPostTourId(postTour.getId()));
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
//        for (PostTour postTour : post.getTours()) {
//            Tour tour = postTour.getTour();
//            TourDto tourDto = new TourDto();
//            tourDto.setTour_id(tour.getId());
//            tourDto.setTitleTour(tour.getTitle());
//            tourDto.setPrice(tour.getPrice());
//            tourDtos.add(tourDto);
//        }
        postDto.setTourDtoList(tourDtos);

        List<String> imagePost = tourDtos.stream()
                .map(TourDto::getImageTour).collect(Collectors.toList());
        postDto.setImagePost(imagePost);

        return postDto;
    }


    @Override
    public List<PostDto> convertListPost(List<Post> posts) {
        return posts.stream().map(post -> {
            PostDto postDto = new PostDto();
            postDto.setPostId(post.getId());
            postDto.setOwnerPostId(post.getUsers().getId());
            postDto.setFullNameUser(post.getUsers().getFirstname() + ' ' + post.getUsers().getLastname());
            postDto.setTitle(post.getTitle());
            postDto.setStart_time(post.getStartTime());
            postDto.setEnd_time(post.getEndTime());
//            postDto.setIsDelete(post.getIsDelete());
            List<TourDto> tourDtos = convertTour(tourRepository.findToursByPostId(post.getId()));
            for (PostTour postTour : post.getTours()) {
                Tour tour = postTour.getTour();
                TourDto tourDto = new TourDto();
                tourDto.setTour_id(tour.getId());
                tourDto.setTitleTour(tour.getTitle());
                tourDto.setPrice(tour.getPrice());
                tourDtos.add(tourDto);
            }
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

            Double averageNumberOfComments = commentRepository.findAverageNumberOfCommentsByPostId(post.getId());
            postDto.setRateAvg(averageNumberOfComments);

            return postDto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<TourDto> convertTour(List<Tour> tours) {
        return tours.stream().map(tour -> {
            TourDto tourDto = new TourDto();
            tourDto.setTour_id(tour.getId());
            tourDto.setTitleTour(tour.getTitle());
            tourDto.setDescription(tour.getDescription());
            tourDto.setImageTour(tour.getImage_tour());
            tourDto.setPrice(tour.getPrice());

            List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
            tourDto.setDestiationDtoList(destinationDtos);
            return tourDto;
        }).collect(Collectors.toList());
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

    @Override
    public ResponseEntity<MessageResponse> createNewTour(String titleTour, Double price, String description, List<Long> destinationId, MultipartFile image, String username) {
        try {
            MessageResponse messageResponse = new MessageResponse();
            Optional<Optional<User>> user = Optional.ofNullable(userRepository.findByUsername(username));
            if (user.isEmpty()) {
                messageResponse.setResponseCode("404");
                messageResponse.setMessage("User Not Found");

                return ResponseEntity.ok(messageResponse);
            }
            Tour tour = new Tour();
            tour.setDescription(description);
            tour.setTitle(titleTour);
            tour.setPrice(price);
            Long maxId = tourRepository.getMaxId();
            System.out.println("Get Max Id" + maxId);
            String nameImage = (maxId + 1) + "_" + image.getOriginalFilename();
            fileStoreService.saveImage(image, maxId + 1, "tours");
            tour.setImage_tour(nameImage);
            tour.setManager(user.get().get());
            tour.setIsDelete(false);
            Set<Destination> destinationSet = destinationId.stream()
                    .map(id -> destinationRepository.findById(id).orElseThrow(() -> new IllegalFormatFlagsException("Destination Not Found with id: " + id)))
                    .collect(Collectors.toSet());
            tour.setDestination(destinationSet);
            tourRepository.save(tour);
            messageResponse.setResponseCode("200");
            messageResponse.setMessage("Create Tour Successfully");
            return ResponseEntity.ok(messageResponse);
        } catch (Exception e) {

            return null;
        }

    }

    public List<TourSchedule> generateTourSchedule(LocalDateTime startTimePost, LocalDateTime endTimePost, List<AddPostTourRequest> addPostTourRequests) {
        List<TourSchedule> schedules = new ArrayList<>();
        // khởi tạo ngày bắt đầu và kết thúc cho tour > startPost 1 tháng và < endPost 1 tuần
        LocalDateTime startTimePostPlusMonth = startTimePost.plusMonths(1);
        LocalDateTime endTimePostMinusWeek = endTimePost.minusWeeks(1);
        // lay danh sach cac tour duoc set ngay
        List<PostTour> existingPostTour = postTourRepository.findAll();
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
            while (isTourOverlapping(existingPostTour, startTour, endTour) || usedDates.contains(startTour)) {
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

    private boolean isTourOverlapping(List<PostTour> existingPostTour, LocalDateTime startTour, LocalDateTime endTour) {
        for (PostTour postTour : existingPostTour) {
            if ((endTour.isEqual(postTour.getStartTimeTour()) || startTour.isAfter(postTour.getStartTimeTour())) &&
                    (startTour.isEqual(postTour.getEndTimeTour()) || startTour.isBefore(postTour.getEndTimeTour())) ||
                    (endTour.isEqual(postTour.getEndTimeTour()) || endTour.isAfter(postTour.getStartTimeTour())) &&
                            (endTour.isEqual(postTour.getEndTimeTour()) || endTour.isBefore(postTour.getEndTimeTour()))) {
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
        List<PostTour> postTourList = new ArrayList<>();
        for (AddPostTourRequest request : addPostRequest.getAddPostTourRequests()) {
            PostTour postTour = new PostTour();
            postTour.setDiscount(request.getDiscount());
            postTour.setQuantity(request.getQuantity());

            Optional<Tour> tourOptional = tourRepository.findById(request.getTourId());
            if (tourOptional.isEmpty()) {
                messageResponse.setResponseCode("404");
                messageResponse.setMessage("Tour Not Found");
                return ResponseEntity.ok(messageResponse);
            }
            postTour.setTour(tourOptional.get());
            LocalDateTime tourStart = addPostRequest.getStartTimePost().plusMonths(1).plusDays(1);
            LocalDateTime tourEnd = tourStart.plusDays(request.getDayTour() - 1);
            if (tourEnd.isAfter(addPostRequest.getEndTimePost().minusWeeks(1))) {
                tourEnd = addPostRequest.getEndTimePost().minusWeeks(1);
            }
            while (isTourOverlapping(postTourList, tourStart, tourEnd)){
                tourStart = tourStart.plusDays(1);
                tourEnd = tourStart.plusDays(request.getDayTour() - 1);
                if (tourEnd.isAfter(addPostRequest.getEndTimePost().minusWeeks(1))) {
                    tourEnd = addPostRequest.getStartTimePost().plusDays(1);
                }
            }
            postTour.setStartTimeTour(tourStart);
            postTour.setEndTimeTour(tourEnd);
            postTour.setPost(post);
            postTourRepository.save(postTour);
            postTourList.add(postTour);

        }
//        post.setTours(new HashSet<>(postTourList));
        postRepository.save(post);
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("Post created successfully");
        return ResponseEntity.ok(messageResponse);
    }
}
