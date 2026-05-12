package onetoone.Posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // Feed — all public posts newest first
    List<Post> findByVisibilityOrderByPublishedAtDesc(String visibility);

    // A specific user's posts newest first
    List<Post> findByAuthor_IdOrderByPublishedAtDesc(int authorId);

    // Public posts by a specific user (their profile feed)
    List<Post> findByAuthor_IdAndVisibilityOrderByPublishedAtDesc(int authorId, String visibility);

    @Query("SELECT p FROM Post p WHERE p.author.userId = :authorId")
    List<Post> findByAuthorUserId(@Param("authorId") int authorId);

    @Query("SELECT p FROM Post p WHERE p.author.userId = :userId OR p.review.user.userId = :userId")
    List<Post> findByAuthorOrReviewUserId(@Param("userId") int userId);

    // Check if a review has already been posted
    Optional<Post> findByReview_Id(int reviewId);

    boolean existsByReview_Id(int reviewId);

    // Posts by media type (pulled from the joined review)
    List<Post> findByReview_MediaTypeOrderByPublishedAtDesc(String mediaType);

    @Transactional
    void deleteByAuthor_Id(int authorId);

    @Transactional
    long deleteByReview_UserIsNull();
}
