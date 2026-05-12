package onetoone;

import onetoone.Admins.AdminRepository;
import onetoone.Notifications.NotificationService;
import onetoone.Posts.Post;
import onetoone.Posts.PostController;
import onetoone.Posts.PostRepository;
import onetoone.Reviews.Review;
import onetoone.Reviews.ReviewRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerPublishTest {

    private MockMvc mockMvc;
    private PostRepository postRepository;
    private ReviewRepository reviewRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);

        PostController controller = new PostController();
        ReflectionTestUtils.setField(controller, "postRepository", postRepository);
        ReflectionTestUtils.setField(controller, "reviewRepository", reviewRepository);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "adminRepository", mock(AdminRepository.class));
        ReflectionTestUtils.setField(controller, "notificationService", notificationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void publishReviewAcceptsEmptyFormRequestFromRestAssured() throws Exception {
        Review review = new Review("MOVIE", "Inception", 80, "placeholder");
        review.setId(95);

        User author = new User("Alex", "alex@iastate.edu", "Password1!");
        author.setId(281);

        when(reviewRepository.findById(95)).thenReturn(review);
        when(userRepository.findByUserId(281)).thenReturn(author);
        when(postRepository.existsByReview_Id(95)).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/reviews/{reviewId}/publish", 95)
                        .queryParam("authorId", "281")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(95))
                .andExpect(jsonPath("$.authorId").value(281))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        verify(notificationService).notify(eq(author), eq("REVIEW"), contains("Inception"));
    }
}
