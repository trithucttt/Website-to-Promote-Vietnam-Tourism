package com.trithuc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trithuc.config.JWTTokenUtil;
import com.trithuc.constant.TravelErrorConstant;
import com.trithuc.dto.*;
import com.trithuc.exception.TravelException;
import com.trithuc.model.*;
import com.trithuc.repository.*;
import com.trithuc.request.*;
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

import java.io.IOException;
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
     */
    @Override
    public ResponseEntity<PaginationResponse> manualPagination(String title, int size, int currentPage) {
        if (title == null || title.isEmpty()) {
            title = "%";
        } else {
            title = "%" + title + "%";
        }
        Pageable pageable = PageRequest.of(currentPage - 1, size);
        Page<Post> postPage = postRepository.findByTitleLikeIgnoreCaseAndIsDeleteFalse(pageable, title);
        List<PostDto> postDtos = convertListPost(postPage.getContent());
        Pagination pagination = Pagination.builder().currentPage(currentPage).size(size).totalPage(postPage.getTotalPages()).totalResult((int) postPage.getTotalElements()).build();
        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setData(postDtos);
        paginationResponse.setPagination(pagination);
        return ResponseEntity.ok(paginationResponse);
    }

    /**
     * Pagination manual.
     */
//    @Override
//    public ResponseEntity<PaginationResponse> manualPagination(String title, int size, int currentPage) {
//        if (title == null || title.isEmpty()) {
//            title = "%";
//        } else {
//            title = "%" + title + "%";
//        }
//        List<Post> posts = postRepository.findAllByTitleLikeIgnoreCaseAndIsDeleteFalse(title);
//        int totalResult = posts.size();
//        int totalPage = (int) Math.ceil((double) totalResult / size);
//        int startIndex = (currentPage - 1) * size;
//        int endIndex = Math.min(startIndex + size, totalResult);
//
//        List<Post> paginationPosts = posts.subList(startIndex, endIndex);
//
//        List<PostDto> postDtos = convertListPost(paginationPosts);
//        Pagination pagination = Pagination.builder()
//                .currentPage(currentPage)
//                .size(size)
//                .totalPage(totalPage)
//                .totalResult(totalResult)
//                .build();
//        PaginationResponse paginationResponse = new PaginationResponse();
//        paginationResponse.setData(postDtos);
//        paginationResponse.setPagination(pagination);
//        return ResponseEntity.ok(paginationResponse);
//
//    }
    @Override
    public ResponseEntity<PaginationResponse> SearchAndFilterAndPagination(String title, LocalDateTime startTime, Integer quantityStart, Integer quantityEnd, Double priceStart, Double priceEnd, Double discountStart, Double discountEnd, Long cityId, String regionName, int size, int currentPage) {
        // Xử lý biến title nếu không có giá trị
        if (title == null || title.isEmpty()) {
            title = "%";
        } else {
            title = "%" + title + "%";
        }

        // Xử lý các tham số tìm kiếm khác: nếu có giá trị thì sẽ đưa vào câu truy vấn, nếu không thì bỏ qua
        LocalDateTime validStartTime = (startTime != null) ? startTime : null;
        Integer validQuantityStart = (quantityStart != null) ? quantityStart : null;
        Integer validQuantityEnd = (quantityEnd != null) ? quantityEnd : null;
        Double validPriceStart = (priceStart != null) ? priceStart : null;
        Double validPriceEnd = (priceEnd != null) ? priceEnd : null;
        Double validDiscountStart = (discountStart != null) ? discountStart : null;
        Double validDiscountEnd = (discountEnd != null) ? discountEnd : null;
        Long validCityId = (cityId != null) ? cityId : null;
        String validRegionName = (regionName != null && !regionName.isEmpty()) ? regionName : null;

        List<Post> posts = postRepository.searchPosts(title, startTime, quantityStart, quantityEnd, priceStart, priceEnd, discountStart, discountEnd, cityId, regionName);

        int totalResult = posts.size();
        int totalPage = (int) Math.ceil((double) totalResult / size);
        int startIndex = (currentPage - 1) * size;
        int endIndex = Math.min(startIndex + size, totalResult);

        List<Post> paginationPosts = posts.subList(startIndex, endIndex);
        // Chuyển đổi danh sách Post thành PostDto
        List<PostDto> postDtos = convertListPost(paginationPosts);

        // Tạo đối tượng Pagination
        Pagination pagination = Pagination.builder().currentPage(currentPage).size(size).totalPage(totalPage).totalResult(totalResult).build();

        // Tạo và trả về kết quả PaginationResponse
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

//    Dùng check post ton tai
    @Override
    public Post getPostById(Long postId) {
        // TODO Auto-generated method stub
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new TravelException(TravelErrorConstant.ERROR_CODE_POST_NOT_FOUND);
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
        List<String> imageUrls = tour.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList());
        tourDto.setImageTour(imageUrls);
        tourDto.setPrice(tour.getPrice());
        List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
        tourDto.setDestiationDtoList(destinationDtos);
        tourDto.setQuantityTour(tour.getQuantity());
        tourDto.setEndTime(tour.getEndTimeTour());
        tourDto.setStartTime(tour.getStartTimeTour());
        tourDto.setDiscount(tour.getDiscount());

        return tourDto;
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

    private TourDto conVertDetailTour(Tour tour) {
        TourDto tourDto = new TourDto();
        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setDescription(tour.getDescription());
//        tourDto.setQuantityTour(tour.getQuantity());
//        tourDto.setImageTour(tour.getImage_tour());
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
        postDto.setIsBusiness(post.getIsBusiness());
        List<TourDto> tourDtos = convertTour(tourRepository.findToursByPostId(post.getId()));
        postDto.setTourDtoList(tourDtos);

// Duyệt qua danh sách tourDtoList và lấy ra danh sách ảnh từ mỗi tour, sau đó gán vào imagePost
        List<String> imagePost = postDto.getTourDtoList().stream().flatMap(tourDto -> tourDto.getImageTour().stream()) // "Phẳng hóa" danh sách các ảnh từ mỗi tour
                .collect(Collectors.toList());
        postDto.setImagePost(imagePost);
        if (!post.getIsBusiness()) {
            List<ImageDTO> imageUrls = post.getImages().stream().map(image -> new ImageDTO(image.getId(), image.getImageUrl())).collect(Collectors.toList());
            postDto.setImagePostEdit(imageUrls);
        }

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
            postDto.setIsBusiness(post.getIsBusiness());
            List<TourDto> tourDtos = convertTour(tourRepository.findToursByPostId(post.getId()));
//            for (Tour tour : post.getTours()) {
//                TourDto tourDto = getTourDto(tour);
//                tourDtos.add(tourDto);
//            }
            postDto.setTourDtoList(tourDtos);
            postDto.setAvatarUser(post.getUsers().getProfileImage());
            Double avgPriceTour = tourDtos.isEmpty() ? 0 : tourDtos.stream().mapToDouble(TourDto::getPrice).average().orElse(0);
            postDto.setPrice(avgPriceTour);
// Duyệt qua danh sách tourDtoList và lấy ra danh sách ảnh từ mỗi tour, sau đó gán vào imagePost
            List<String> imagePost = postDto.getTourDtoList().stream().flatMap(tourDto -> tourDto.getImageTour().stream()) // "Phẳng hóa" danh sách các ảnh từ mỗi tour
                    .collect(Collectors.toList());

            postDto.setImagePost(imagePost);

            List<Double> discounts = tourRepository.findDiscountsByPostId(post.getId());
            double avgDiscount = discounts.isEmpty() ? 0 : discounts.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            postDto.setAvgDiscount(avgDiscount);

//            Double averageNumberOfComments = commentRepository.findAverageNumberOfCommentsByPostId(post.getId());
//            postDto.setRateAvg(averageNumberOfComments);
            if (!post.getIsBusiness()) {
                List<String> imageUrls = post.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList());
                postDto.setImagePost(imageUrls);
            }
            return postDto;
        }).collect(Collectors.toList());
    }

    private TourDto getTourDto(Tour tour) {
        TourDto tourDto = new TourDto();
        // Gán các thông tin khác
        tourDto.setCompanyAvatar(tour.getManager().getProfileImage());
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setPrice(tour.getPrice());
        tourDto.setDiscount(tour.getDiscount());
        tourDto.setStartTime(tour.getStartTimeTour());
        tourDto.setEndTime(tour.getEndTimeTour());
        tourDto.setQuantityTour(tour.getQuantity());
        tourDto.setDescription(tour.getDescription());


        List<String> imageUrls = tour.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList());
        tourDto.setImageTour(imageUrls);


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

    // lấy danh sách tour chuưa thuộc baài viết  SL > 0
    @Override
    public List<TourDto> listTourByToken(String username) {
        Optional<User> user = userRepository.findByUsername(username);
//        return tourRepository.findToursByManager(user.get());
        return convertTour(tourRepository.findAvailableToursByManager(LocalDateTime.now(), user.get().getId()).stream().map(tour -> tour).collect(Collectors.toList()));
    }


    @Override
    public List<Destination> listDestinationByToken(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return destinationRepository.findDestinationsByManager(user.get());
    }

    @Override
    public List<DestinationDto> getTableDestination(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        List<Destination> destinations = destinationRepository.findDestinationsByManager(user.get());
        return destinations.stream().map(destination -> {
            DestinationDto destinationDto = new DestinationDto();
            destinationDto.setDesId(destination.getId());
            destinationDto.setDesName(destination.getName());
            destinationDto.setDesImage(destination.getImage_destination());
            destinationDto.setDesAddress(destination.getAddress());
            destinationDto.setDescription(destination.getDescription());
            destinationDto.setWardId(destination.getWard().getId());
            destinationDto.setCityId(destination.getWard().getDistrict().getCity().getId());
            destinationDto.setDistrictId(destination.getWard().getDistrict().getId());
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
                throw new TravelException(TravelErrorConstant.USER_NOT_FOUND);
            }
            Destination destination = new Destination();
            destination.setName(name);
            destination.setAddress(address);
            destination.setDescription(description);

            destination.setManager(user.get().get());
            Optional<Ward> ward = wardRepository.findById(wardId);
            if (ward.isEmpty()) {
                throw new TravelException(TravelErrorConstant.WARD_NOT_FOUND);
            }
            destination.setWard(ward.get());

            Long maxId = destinationRepository.getMaxId();
            System.out.println("Get Max Id" + maxId);
            String nameImage = (maxId + 1) + "_" + image.getOriginalFilename();
            destination.setImage_destination(nameImage);
            fileStoreService.saveImage(image, maxId + 1, "destinations");
            destinationRepository.save(destination);
            return "Thêm mới điểm đến thành công";
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    @Override
    public MessageResponse updateDestination(String data, MultipartFile image, String username) {
        try {
            // Kiểm tra xem người dùng có tồn tại không
            UpdateDestinationRequest request = convertJsonToObject(data, UpdateDestinationRequest.class);
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                throw new TravelException(TravelErrorConstant.USER_NOT_FOUND);
            }

            // Tìm Destination cần cập nhật
            Optional<Destination> destinationOpt = destinationRepository.findById(request.getDestinationId());
            if (destinationOpt.isEmpty()) {
                throw new TravelException(TravelErrorConstant.DESTINATION_NOT_FOUND);
            }

            Destination destination = destinationOpt.get();

            // Cập nhật thông tin cơ bản
            destination.setName(request.getDestinationName());
            destination.setAddress(request.getAddress());
            destination.setDescription(request.getDescription());
            destination.setManager(user.get());

            Optional<Ward> ward = wardRepository.findById(request.getWardId());
            if (ward.isEmpty()) {
                throw new TravelException(TravelErrorConstant.WARD_NOT_FOUND);
            }
            destination.setWard(ward.get());

            // Nếu có ảnh mới, cập nhật ảnh
            if (image != null && !image.isEmpty()) {
                fileStoreService.deleteOldImage("destinations", destinationOpt.get().getImage_destination());
                destinationRepository.save(destination);
                // Tạo tên mới cho ảnh
                String nameImage = request.getDestinationId() + "_" + image.getOriginalFilename();
                destination.setImage_destination(nameImage);
                fileStoreService.saveImage(image, request.getDestinationId(), "destinations");
            }

            // Lưu các thay đổi vào cơ sở dữ liệu
            destinationRepository.save(destination);
            return setUpResponse("Cập nhật điểm đến thành công", "201", null);
        } catch (Exception e) {
            throw new TravelException(TravelErrorConstant.UPDATE_DESTINATION_FAILED_SERVER_ERROR);
        }
    }


    @Override
    public MessageResponse setUpResponse(String message, String responseCode, Object data) {
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
    public MessageResponse createNewTour(String addTourRequest, List<MultipartFile> images) throws JsonProcessingException {

        AddTourRequest request = convertJsonToObject(addTourRequest, AddTourRequest.class);
        Optional<User> userOptional = userRepository.findById(request.getManagerId());
        if (userOptional.isEmpty()) {
            throw new TravelException(TravelErrorConstant.USER_NOT_FOUND);
        }
        User user = userOptional.get();
        Set<Destination> destinations = request.getDestinationIds().stream().map(id -> destinationRepository.findById(id).orElseThrow(() -> new IllegalFormatFlagsException("Destination Not Found with id: " + id))).collect(Collectors.toSet());
        Tour newTour = new Tour();

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
        Tour saveTour = tourRepository.save(newTour);
        if (!images.isEmpty()) {
            List<Image> imageToSave = new ArrayList<>();
            images.forEach(image -> {
                Image newImageTour = new Image();
                newImageTour.setEntityType("tour");
                newImageTour.setTour(saveTour);
                try {
                    newImageTour.setImageUrl(fileStoreService.saveImageCloudinary(image));
                } catch (IOException e) {
                    throw new TravelException(TravelErrorConstant.SAVE_IMAGE_FAILED);
                }
                imageToSave.add(newImageTour);
            });
            imageRepository.saveAll(imageToSave);
        }
        return setUpResponse("Thêm mới chuyến đi thành công", "200", null);
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
            if ((endTour.isEqual(tour.getStartTimeTour()) || startTour.isAfter(tour.getStartTimeTour())) && (startTour.isEqual(tour.getEndTimeTour()) || startTour.isBefore(tour.getEndTimeTour())) || (endTour.isEqual(tour.getEndTimeTour()) || endTour.isAfter(tour.getStartTimeTour())) && (endTour.isEqual(tour.getEndTimeTour()) || endTour.isBefore(tour.getEndTimeTour()))) {
                return true;
            }
        }
        return false;
    }

    // create bussiness post
    @Override
    @Transactional
    public ResponseEntity<MessageResponse> createNewPost(AddPostRequest addPostRequest, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.ok(setUpResponse("Không tìm thấy người dùng", "404", null));
        }
        User user = userOptional.get();
        Post post = new Post();
        post.setTitle(addPostRequest.getTitlePost());
        post.setStartTime(LocalDateTime.now());
        post.setEndTime(addPostRequest.getEndDay());
        post.setIsDelete(false);
        post.setUsers(user);
        post.setIsBusiness(true);
        List<Tour> tours = tourRepository.findAllById(addPostRequest.getTourId());
        post.setTours(new HashSet<>(tours));
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
        postRepository.save(post);
        return ResponseEntity.ok(setUpResponse("Thêm bài viết thành công", "200", null));
    }

    @Override
    public MessageResponse deleteTourById(Long tourId) {
        Optional<Tour> tour = tourRepository.findById(tourId);
        if (tour.isPresent()) {
            // Kiểm tra xem tour có thuộc bài đăng
            if (tour.get().getPost() != null) {
                // Nếu tour có trong bài đăng, không được xóa
                return setUpResponse("Chuyến tham quan được liên kết với một bài đăng và không thể xóa", "400", null);
            }
            // Nếu tour không nằm trong bài đăng nào -> xóa
            tour.get().setIsDelete(true);
            tourRepository.save(tour.get());
            return setUpResponse("Xóa chuyến đi thành coông", "200", null);
        }
        throw new TravelException(TravelErrorConstant.DELETE_FAILED);
    }

    @Override
    public EditTourDTO getTourById(Long tourId) {
        Optional<Tour> tourOptional = tourRepository.findById(tourId);
        if (tourOptional.isEmpty()) {
            throw new TravelException(TravelErrorConstant.ERROR_CODE_TOUR_NOT_FOUND);
        }
        Tour tour = tourOptional.get();
        EditTourDTO tourDto = new EditTourDTO();
        // Gán các thông tin khác
        tourDto.setCompanyAvatar(tour.getManager().getProfileImage());
        tourDto.setCompanyTour(tour.getManager().getFirstname() + " " + tour.getManager().getLastname());
        tourDto.setTour_id(tour.getId());
        tourDto.setTitleTour(tour.getTitle());
        tourDto.setPrice(tour.getPrice());
        tourDto.setDiscount(tour.getDiscount());
        tourDto.setStartTime(tour.getStartTimeTour());
        tourDto.setEndTime(tour.getEndTimeTour());
        tourDto.setQuantityTour(tour.getQuantity());
        tourDto.setDescription(tour.getDescription());

        List<ImageDTO> imageDtos = tour.getImages().stream().map(image -> new ImageDTO(image.getId(), image.getImageUrl())) // Tạo đối tượng ImageDto
                .collect(Collectors.toList());

        tourDto.setImageTour(imageDtos);


        List<DestinationDto> destinationDtos = convertDestination(destinationRepository.findDestinationsByTourId(tour.getId()));
        tourDto.setDestiationDtoList(destinationDtos);

        return tourDto;
    }

    public Tour findTourById(Long tourId) {
        Optional<Tour> tour = tourRepository.findById(tourId);
        if (tour.isEmpty()) {
            throw new TravelException(TravelErrorConstant.ERROR_CODE_TOUR_NOT_FOUND);
        }
        return tour.get();
    }


    @Override
    public List<TourDto> getToursByPostId(Long postId) {
        Post post = getPostById(postId);
        return tourRepository.findByPostId(postId).stream().map(this::getTourDto).toList();
    }

    @Override
    public MessageResponse deletePostById(Long postId) {

        Optional<Post> post = postRepository.findById(postId);
        if (post.isPresent()) {
            post.get().setIsDelete(true);
            postRepository.save(post.get());
            return setUpResponse("Delete Post successfully", "200", null);
        }
        throw new TravelException(TravelErrorConstant.DELETE_FAILED);
    }

    private boolean compareTourLists(List<Long> oldTours, List<Long> newTours) {
        if (oldTours == null || newTours == null) {
            return oldTours == newTours;
        }
        if (oldTours.size() != newTours.size()) {
            return false;
        }
        return new HashSet<>(oldTours).equals(new HashSet<>(newTours));
    }

    private Set<Tour> getUpdatedToursFromIds(List<Long> tourIds) {
        Set<Tour> tours = new HashSet<>();
        for (Long id : tourIds) {
            Tour tour = findTourById(id);
            if (tour != null) {
                tours.add(tour);
            }
        }
        return tours;
    }

    @Override
    public MessageResponse editBusinessPost(EditPostRequest request) {
        Post post = getPostById(request.getPostId());
        String oldTitlePost = post.getTitle();
        Set<Tour> oldTours = post.getTours();
        List<Long> oldTourIds = oldTours.stream().map(Tour::getId).toList();
        System.out.println("title post change: " + request.getTitle());
        System.out.println("Danh sách tour post change: " + request.getTours());

        String newTitlePost = request.getTitle();
        List<Long> newTourIds = request.getTours();

        // Kiểm tra thay đổi tiêu đề và danh sách tours
        boolean isTitleChanged = !Objects.equals(oldTitlePost, newTitlePost);
        boolean areToursChanged = !compareTourLists(oldTourIds, newTourIds);

        if (!isTitleChanged && !areToursChanged) {
            return setUpResponse("Không có gì thay đổi", "204", null);
        }

        // Cập nhật tiêu đề nếu thay đổi
            post.setTitle(newTitlePost);
        // Cập nhật tours nếu thay đổi
        if (areToursChanged) {
            Set<Tour> updatedTours = getUpdatedToursFromIds(newTourIds);

            // Xóa mối quan hệ cũ: đặt post của các tour cũ thành null
            oldTours.forEach(tour -> tour.setPost(null));

            // Lưu các tour cũ đã ngắt kết nối với post
            tourRepository.saveAll(oldTours);  // Lưu các tour đã ngắt kết nối với post

            // Thêm các tour mới vào post và thiết lập mối quan hệ
            updatedTours.forEach(tour -> tour.setPost(post)); // Gán lại post cho tour mới

            // Thêm các tour mới vào danh sách tours của post
            post.getTours().addAll(updatedTours);

            // Lưu lại post sau khi cập nhật danh sách tours
            postRepository.save(post);
            System.out.println("Tour IDs cũ: " + oldTourIds);
            System.out.println("Tour IDs mới: " + newTourIds);
            System.out.println("Tours đã được cập nhật: " + updatedTours.stream().map(Tour::getId).toList());

        }

        return setUpResponse("Đã chỉnh sửa thành công", "200", null);
    }

    @Override
    public MessageResponse editBusinessTour(String editTourRequest, List<MultipartFile> editImages) throws JsonProcessingException {
        EditTourRequest request = convertJsonToObject(editTourRequest, EditTourRequest.class);

        // Tìm tour theo ID
        Optional<Tour> optionalTour = tourRepository.findById(request.getTourId());
        if (optionalTour.isEmpty()) {
            return setUpResponse("Tour không tồn tại.", "400", null);
        }
        System.out.println("Data from client" + request);
        Tour existingTour = optionalTour.get();
        boolean hasChange = false;
        StringBuilder changeLog = new StringBuilder("Các trường đã thay đổi:\n");

        // Kiểm tra từng trường và so sánh với giá trị hiện tại trong Tour
        if (!request.getEditTitle().equals(existingTour.getTitle())) {
            changeLog.append("Tiêu đề: ").append(existingTour.getTitle()).append(" -> ").append(request.getEditTitle()).append("\n");
            existingTour.setTitle(request.getEditTitle());
            hasChange = true;
        }

        if (!request.getEditDescription().equals(existingTour.getDescription())) {
            changeLog.append("Mô tả: đã thay đổi\n");
            existingTour.setDescription(request.getEditDescription());
            hasChange = true;
        }

        if (!request.getEditPrice().equals(existingTour.getPrice())) {
            changeLog.append("Giá: ").append(existingTour.getPrice()).append(" -> ").append(request.getEditPrice()).append("\n");
            existingTour.setPrice(request.getEditPrice());
            hasChange = true;
        }

        if (!request.getEditQuantity().equals(existingTour.getQuantity())) {
            changeLog.append("Số lượng: ").append(existingTour.getQuantity()).append(" -> ").append(request.getEditQuantity()).append("\n");
            existingTour.setQuantity(request.getEditQuantity());
            hasChange = true;
        }

        if (!request.getEditDiscount().equals(existingTour.getDiscount())) {
            changeLog.append("Giảm giá: ").append(existingTour.getDiscount()).append(" -> ").append(request.getEditDiscount()).append("\n");
            existingTour.setDiscount(request.getEditDiscount());
            hasChange = true;
        }

        if (!request.getEditStartTime().equals(existingTour.getStartTimeTour())) {
            changeLog.append("Thời gian bắt đầu: ").append(existingTour.getStartTimeTour()).append(" -> ").append(request.getEditStartTime()).append("\n");
            existingTour.setStartTimeTour(request.getEditStartTime());
            hasChange = true;
        }

        if (!request.getEditEndTime().equals(existingTour.getEndTimeTour())) {
            changeLog.append("Thời gian kết thúc: ").append(existingTour.getEndTimeTour()).append(" -> ").append(request.getEditEndTime()).append("\n");
            existingTour.setEndTimeTour(request.getEditEndTime());
            hasChange = true;
        }

        // Lấy danh sách ID điểm đến hiện tại từ Tour
        Set<Long> existingDestinationIds = existingTour.getDestination().stream()
                .map(Destination::getId)
                .collect(Collectors.toSet());

        // Lấy danh sách Destination từ request
        Set<Destination> destinations = request.getEditDestination().stream()
                .map(id -> destinationRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Destination Not Found with id: " + id)))
                .collect(Collectors.toSet());

        // Lấy danh sách ID điểm đến mới từ request
        Set<Long> newDestinationIds = destinations.stream()
                .map(Destination::getId)
                .collect(Collectors.toSet());

        // Tìm điểm đến cần thêm
        Set<Long> destinationsToAdd = newDestinationIds.stream()
                .filter(id -> !existingDestinationIds.contains(id))
                .collect(Collectors.toSet());

        // Tìm điểm đến cần xóa
        Set<Long> destinationsToRemove = existingDestinationIds.stream()
                .filter(id -> !newDestinationIds.contains(id))
                .collect(Collectors.toSet());

        // Nếu có thay đổi, cập nhật danh sách
        if (!destinationsToAdd.isEmpty() || !destinationsToRemove.isEmpty()) {
            changeLog.append("Điểm đến: đã thay đổi\n");

            // Xóa các điểm đến không còn trong danh sách
            existingTour.getDestination().removeIf(destination -> destinationsToRemove.contains(destination.getId()));

            // Thêm các điểm đến mới
            destinationsToAdd.forEach(id -> {
                Destination destination = destinationRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Destination Not Found with id: " + id));
                existingTour.getDestination().add(destination);
            });

            // Lưu cập nhật
            tourRepository.save(existingTour);
            hasChange = true;
        }


        // Xử lý phần ảnh
        List<Long> OldImageId = imageRepository.findByTourId(request.getTourId()).stream().map(Image::getId).toList(); // Chuyển thành danh sách

        List<Long> updateImages = request.getUpdateImageIds(); // Danh sách ID hình ảnh cập nhật

        // Lọc các ID của hình ảnh cũ mà không nằm trong danh sách hình ảnh được cập nhật
        List<Long> idImageToDelete = OldImageId.stream().filter(id -> !updateImages.contains(id)) // Kiểm tra trực tiếp trong danh sách
                .collect(Collectors.toList()); // Chuyển thành danh sách
        (imageRepository.findByTourId(request.getTourId())).removeIf(image -> idImageToDelete.contains(image.getId()));
        imageRepository.deleteAllById(idImageToDelete);
        tourRepository.save(existingTour);

        if (!(editImages == null)) {
            changeLog.append("Ảnh: đã thay đổi\n");
            // Logic xử lý cập nhật ảnh

            List<Image> imageToSave = new ArrayList<>();
            editImages.forEach(image -> {
                Image newImageTour = new Image();
                newImageTour.setEntityType("tour");
                newImageTour.setTour(existingTour);
                try {
                    newImageTour.setImageUrl(fileStoreService.saveImageCloudinary(image));
                } catch (IOException e) {
                    throw new TravelException(TravelErrorConstant.SAVE_IMAGE_FAILED);
                }
                imageToSave.add(newImageTour);
            });
            imageRepository.saveAll(imageToSave);
            tourRepository.save(existingTour);
            hasChange = true;
        }

        // Lưu tour đã cập nhật nếu có thay đổi
        if (hasChange) {
            tourRepository.save(existingTour);
            return setUpResponse(changeLog.toString(), "200", null);
        }

        // Nếu không có thay đổi
        return setUpResponse("Không có trường nào thay đổi.", "200", null);
    }

    @Override
    public MessageResponse createPostNonSale(String data, List<MultipartFile> images) throws JsonProcessingException {
        CreatePostNonSale createPostNonSale = convertJsonToObject(data, CreatePostNonSale.class);
        Optional<User> user = userRepository.findById(createPostNonSale.getUserId());
        if (user.isEmpty()) {
            return setUpResponse("User not found", "400", null);
        }
        Post post = new Post();
        post.setUsers(user.get());
        post.setStartTime(LocalDateTime.now());
        post.setTitle(createPostNonSale.getContent());
        post.setIsBusiness(false);
        post.setIsDelete(false);
        Post savePost = postRepository.save(post);
        if (!images.isEmpty()) {
            List<Image> imageToSave = new ArrayList<>();
            images.forEach(image -> {
                Image newImagePost = new Image();
                newImagePost.setEntityType("post");
                newImagePost.setPost(savePost);
                try {
                    newImagePost.setImageUrl(fileStoreService.saveImageCloudinary(image));
                } catch (IOException e) {
                    throw new TravelException(TravelErrorConstant.SAVE_IMAGE_FAILED);
                }
                imageToSave.add(newImagePost);
            });
            imageRepository.saveAll(imageToSave);
        }
        return setUpResponse("Tạo mới bài viết thành công", "200", null);
    }

    private boolean compareImageIds(List<Long> oldImageIds, List<Long> newImageIds) {
        // Kiểm tra nếu một trong hai danh sách là null, nhưng danh sách kia thì không
        if (oldImageIds == null && newImageIds == null) {
            return true;
        }
        if (oldImageIds == null || newImageIds == null) {
            return false;
        }

        // Kiểm tra kích thước của cả hai danh sách
        if (oldImageIds.size() != newImageIds.size()) {
            return false;
        }

        // Tạo bản sao để tránh thay đổi dữ liệu gốc, sau đó sắp xếp
        List<Long> sortedOldImageIds = new ArrayList<>(oldImageIds);
        List<Long> sortedNewImageIds = new ArrayList<>(newImageIds);

        Collections.sort(sortedOldImageIds);
        Collections.sort(sortedNewImageIds);

        // So sánh từng phần tử trong danh sách đã sắp xếp
        return sortedOldImageIds.equals(sortedNewImageIds);
    }

    private Set<Image> getUpdatedImagesFromIds(List<Long> newImageIds) {
        // Kiểm tra nếu danh sách ID ảnh mới là null hoặc rỗng, trả về tập rỗng
        if (newImageIds == null || newImageIds.isEmpty()) {
            return new HashSet<>();
        }

        // Lấy danh sách ảnh từ CSDL dựa trên danh sách ID ảnh
        List<Image> images = imageRepository.findAllById(newImageIds);

        // Kiểm tra xem số lượng ảnh lấy được có trùng với số ID không (để xác minh ảnh có tồn tại)
        if (images.size() != newImageIds.size()) {
            throw new TravelException(TravelErrorConstant.IMAGE_NOT_FOUND);
        }

        // Trả về tập hợp các ảnh đã lấy được
        return new HashSet<>(images);
    }

    @Override
    public MessageResponse EditPostNonSale(String data, List<MultipartFile> editImages) throws JsonProcessingException {
        // Xác thực postId có tồn tại không
        EditPostNonSaleRequest request = convertJsonToObject(data, EditPostNonSaleRequest.class);
        Post post = getPostById(request.getPostId());
        if (post == null) {
            return setUpResponse("Bài viết không tồn tại", "404", null);
        }

        // Lấy thông tin từ bài viết hiện tại
        String oldTitlePost = post.getTitle();
        List<Long> oldImageIds = post.getImages().stream().map(Image::getId).toList(); // Lấy danh sách ID ảnh hiện tại

        String newTitlePost = request.getTitle();
        List<Long> requestOldImageIds = request.getOldImageIds(); // Danh sách ID ảnh cũ từ yêu cầu

        boolean hasChange = false;
        StringBuilder changeLog = new StringBuilder();

        // Kiểm tra sự thay đổi của title
        if (!oldTitlePost.equals(newTitlePost)) {
            post.setTitle(newTitlePost);
            changeLog.append("Tiêu đề: đã thay đổi\n");
            hasChange = true;
        }

        // Kiểm tra sự thay đổi của danh sách ID ảnh cũ
        if (!compareImageIds(oldImageIds, requestOldImageIds)) {
            List<Image> updatedImages = getUpdatedImagesFromIds(requestOldImageIds).stream().toList();
            post.setImages(updatedImages);
            changeLog.append("Ảnh cũ: đã thay đổi\n");
            hasChange = true;
        }

        // Thêm các ảnh mới từ danh sách MultipartFile nếu có
        if (editImages != null && !editImages.isEmpty()) {
            changeLog.append("Ảnh mới: đã thay đổi\n");

            List<Image> imageToSave = new ArrayList<>();
            editImages.forEach(image -> {
                Image newImage = new Image();
                newImage.setEntityType("post");
                newImage.setPost(post);
                try {
                    newImage.setImageUrl(fileStoreService.saveImageCloudinary(image)); // Lưu ảnh vào Cloudinary
                } catch (IOException e) {
                    throw new TravelException(TravelErrorConstant.SAVE_IMAGE_FAILED);
                }
                imageToSave.add(newImage);
            });

            // Lưu các ảnh mới vào cơ sở dữ liệu và cập nhật bài viết
            imageRepository.saveAll(imageToSave);
            post.getImages().addAll(imageToSave);
            hasChange = true;
        }

        // Lưu bài viết nếu có thay đổi
        if (hasChange) {
            postRepository.save(post);
            return setUpResponse("Đã chỉnh sửa thành công", "200", null);
        } else {
            return setUpResponse("Không có gì thay đổi", "201", null);
        }
    }

    @Override
    public MessageResponse deleteDestinationById(Long destinationId) {
        try {
            // Tìm điểm đến theo ID
            Optional<Destination> destinationOptional = destinationRepository.findById(destinationId);
            if (destinationOptional.isEmpty()) {
                throw new TravelException(TravelErrorConstant.DESTINATION_NOT_FOUND);
            }

            Destination destination = destinationOptional.get();

            // Xóa ảnh cũ khỏi server
            String imageName = destination.getImage_destination();
            fileStoreService.deleteOldImage("destinations", imageName);

            // Xóa khỏi cơ sở dữ liệu
            destinationRepository.delete(destination);

            return   setUpResponse("Đã xóa thành công", "200", null);
        } catch (Exception e) {
            return setUpResponse("Xóa điểm đến thất bại" + e.getMessage(), "400", null);
        }
    }



}
