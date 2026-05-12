package onetoone.Reviews;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import onetoone.Users.User;
import onetoone.Users.UserRepository;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

@RestController
public class ReviewController {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;
    
    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/reviews")
    List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping(path = "/reviews/{id}")
    Review getReviewById(@PathVariable int id) {
        return reviewRepository.findById(id);
    }

    @GetMapping("/reviews/type/{mediaType}")
    List<Review> getReviewsByType(@PathVariable String mediaType) {
        return reviewRepository.findByMediaType(mediaType.toUpperCase());
    }

    @PostMapping(path = "/reviews")
    String createReview(@RequestBody Review review) {
        if (review == null) return failure;

        if (review.getMediaType() != null) {
            review.setMediaType(review.getMediaType().trim().toUpperCase());
        }

        reviewRepository.save(review);
        return success;
    }

    @PutMapping("/reviews/{id}")
    public Review updateReview(@PathVariable int id, @RequestBody Review request) {

        Review existing = reviewRepository.findById(id);

        if (existing == null) {
            throw new RuntimeException("Review id does not exist");
        }

        if (request.getId() != id) {
            throw new RuntimeException("Path id does not match request id");
        }

        existing.setMediaType(request.getMediaType());
        existing.setTitle(request.getTitle());
        existing.setRating(request.getRating());
        existing.setBody(request.getBody());

        reviewRepository.save(existing);

        return existing;
    }

    @PutMapping("/reviews/{reviewId}/user/{userId}")
    String assignReviewToUser(@PathVariable int reviewId, @PathVariable int userId) {
        Review review = reviewRepository.findById(reviewId);
        User user = userRepository.findById(userId);

        if (review == null || user == null) return failure;

        review.setUser(user);
        reviewRepository.save(review);
        return success;
    }

    @DeleteMapping(path = "/reviews/{id}")
    String deleteReview(@PathVariable int id) {
        reviewRepository.deleteById(id);
        return success;
    }
}
