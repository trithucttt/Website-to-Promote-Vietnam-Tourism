//package com.trithuc.service;
//
//import com.trithuc.dto.PostDto;
//import com.trithuc.model.Post;
//import com.trithuc.model.User;
//import com.trithuc.repository.PostRepository;
//import com.trithuc.repository.TourRepository;
//import com.trithuc.repository.UserRepository;
//import com.trithuc.service.impl.TravelContentServiceImpl;
//import jakarta.persistence.EntityNotFoundException;
//import org.hibernate.annotations.NotFound;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class TravelContentServiceTest {
//    @Mock
//    PostRepository postRepository;
//    @Mock
//    TourRepository tourRepository;
//    @InjectMocks
//    TravelContentServiceImpl travelContentService;
//    private List<Post> mockPosts;
//
//    private UserRepository userRepository;
//    private List<PostDto> expectedPostDtos;
//    @BeforeEach
//    void setUp() {
//        // Tạo dữ liệu mô phỏng
//        mockPosts = Arrays.asList(
//                new Post(1L, "Title1", new User()),
//                new Post(2L, "Title2", new User())
//        );
//
//// Now, set the properties of User objects
//        mockPosts.get(0).getUsers().setId(1L);
//        mockPosts.get(0).getUsers().setFirstname("First1");
//        mockPosts.get(0).getUsers().setLastname("Last1");
//        mockPosts.get(0).getUsers().setProfileImage("image1");
//
//        mockPosts.get(1).getUsers().setId(2L);
//        mockPosts.get(1).getUsers().setFirstname("First2");
//        mockPosts.get(1).getUsers().setLastname("Last2");
//        mockPosts.get(1).getUsers().setProfileImage("image2");
//
//        // Tạo danh sách PostDto mong đợi tương ứng
//        expectedPostDtos = Arrays.asList(
//                new PostDto(/* Initialize with expected values */),
//                new PostDto(/* Initialize with expected values */)
//        );
//    }
//
//    @Test
//    void testGetAllPost() {
//        // Given
//        List<Post> mockPosts = Arrays.asList(
//                new Post(1L, "Title1", new User()),
//                new Post(2L, "Title2", new User())
//        );
//        when(postRepository.findAll()).thenReturn(mockPosts);
//        when(travelContentService.convertListPost(mockPosts)).thenReturn(Collections.emptyList());
//
//
//        // When
//        List<PostDto> actualPostDtos = travelContentService.getAllPost();
//
//        // Then
//        assertNotNull(actualPostDtos);
//        assertEquals(2, actualPostDtos.size());
//        verify(postRepository).findAll();
//        //verify(travelContentServiceMock).convertListPost(mockPosts);
//    }
//
//
//
//    @Test
//    void testConvertListPost() {
//        // Cấu hình mô phỏng cho các phương thức tương tác với database
//        when(tourRepository.findToursByPostId(anyLong())).thenReturn(new ArrayList<>());  // Tùy chỉnh phù hợp
//        when(tourRepository.findDiscountsByPostId(anyLong())).thenReturn(Arrays.asList(10.0, 15.0));
//
//        // Gọi phương thức cần test
//        List<PostDto> result = travelContentService.convertListPost(mockPosts);
//
//        // Kiểm tra kết quả
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("Title1", result.get(0).getTitle());
//        assertEquals(12.5, result.get(0).getAvgDiscount());
//
//        // Kiểm tra liệu các phương thức trên repository có được gọi đúng không
//        verify(tourRepository, times(mockPosts.size())).findToursByPostId(anyLong());
//        verify(tourRepository, times(mockPosts.size())).findDiscountsByPostId(anyLong());
//    }
//
//
//    @Test
//    public void testWhenGePostByIdWithSuccessPost(){
//        Post mockPosts = new Post(1L, "Title1", new User());
//        mockPosts.getUsers().setId(2L);
//        mockPosts.getUsers().setFirstname("First2");
//        mockPosts.getUsers().setLastname("Last2");
//        mockPosts.getUsers().setProfileImage("image2");
//        when(postRepository.findById(1L)).thenReturn(Optional.of(mockPosts));
//
//        Post result = travelContentService.getPostById(1L);
//        assertEquals(result.getId(),mockPosts.getId());
//        verify(postRepository,times(1)).findById(1L);
//
//    }
//
//    @Test
//    public void testWhenGetPostByIdWithFailedException(){
//      Long invalidIdPost = 2L;
//
//       when(postRepository.findById(any(Long.class))).thenReturn(Optional.empty());
//     assertThatThrownBy(() -> travelContentService.getPostById(invalidIdPost))
//             .isInstanceOf(EntityNotFoundException.class)
//             .hasMessageContaining("Post not found");
//     verify(postRepository).findById(invalidIdPost);
//    }
//
//// create Destination
//    @Mock
//    private MultipartFile imageTest;
//    @Test
//    void testCreateDestinationWithUserNotFound(){
//        String validUsername = "ValidUsername";
//        when(userRepository.findByUsername(validUsername)).thenReturn(null);
//        String result = travelContentService.createDestination("Test DestinationName","Test address",1L,"Test Description",imageTest,validUsername);
//        assertEquals("User Not Found",result);
//    }
//
//}
