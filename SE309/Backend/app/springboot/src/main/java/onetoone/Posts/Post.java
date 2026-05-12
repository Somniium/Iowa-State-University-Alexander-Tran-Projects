package onetoone.Posts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Comments.Comment;
import onetoone.Likes.Like;
import onetoone.Reviews.Review;
import onetoone.Users.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Post is the social/published layer on top of a Review.
 *
 * Separation of concerns:
 *   Review  = the data  (rating, body, media link) — written privately
 *   Post    = the event (published to the feed, open to comments and likes)
 *
 * A user drafts a Review, then publishes it as a Post.
 * Comments and Likes attach to the Post, not the Review.
 */
@Entity
@Table(name = "posts")
@Schema(description = "A published review post. The social layer on top of a Review — comments and likes attach here.")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique post ID", example = "1")
    private int id;

    /** The review this post is publishing. One review → at most one post. */
    @OneToOne
    @JoinColumn(name = "review_id", unique = true)
    @JsonIgnore
    private Review review;

    /** The user who published this post. */
    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    /**
     * Optional extra text the user adds when posting —
     * like an Instagram caption on top of the review itself.
     */
    @Column(length = 500)
    @Schema(description = "Optional caption the user adds when publishing", example = "Finally finished this one — had to share my thoughts!")
    private String caption;

    /** PUBLIC = visible to everyone, FRIENDS = visible to followers only (future use) */
    @Schema(description = "Visibility: PUBLIC or FRIENDS", example = "PUBLIC")
    private String visibility = "PUBLIC";

    @Schema(description = "When the post was published", example = "2024-11-01T14:30:00")
    private LocalDateTime publishedAt;

    @Schema(description = "When the post was last edited", example = "2024-11-02T09:00:00")
    private LocalDateTime updatedAt;

    /**
     * Comments on this post. LAZY so /feed doesn't outer-join every comment row
     * against every like row (cartesian product → N*M rows for a single post).
     * getCommentCount() still works via OSIV during HTTP serialization.
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    /** Likes on this post. LAZY for the same reason as comments above. */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Like> likes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Post() {}

    public Post(Review review, User author, String caption, String visibility) {
        this.review = review;
        this.author = author;
        this.caption = caption;
        this.visibility = visibility != null ? visibility.toUpperCase() : "PUBLIC";
    }

    // ---- Convenience JSON fields (avoids circular serialization) ----

    @Schema(description = "ID of the Review", example = "3")
    public Integer getReviewId()   { return review != null ? review.getId()        : null; }

    @Schema(description = "ID of the author (User)", example = "42")
    public Integer getAuthorId()   { return author != null ? author.getId()        : null; }

    @Schema(description = "Display name of the author", example = "Alex Tran")
    public String  getAuthorName() { return author != null ? author.getName()      : null; }

    @Schema(description = "Title of the reviewed item (from the linked Review)", example = "Inception")
    public String  getMediaTitle() { return review != null ? review.getTitle()     : null; }

    @Schema(description = "Media type of the reviewed item (from the linked Review)", example = "MOVIE")
    public String  getMediaType()  { return review != null ? review.getMediaType() : null; }

    @Schema(description = "Metacritic-style score from the linked Review (0-100)", example = "92")
    public Integer getRating()     { return review != null ? review.getRating()    : null; }

    @Schema(description = "Total number of comments on this post", example = "4")
    public int getCommentCount()   { return comments.size(); }

    @Schema(description = "Total number of likes on this post", example = "12")
    public int getLikeCount()      { return likes.size(); }

    // ---- Getters & Setters ----

    public int getId() { return id; }
    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @JsonIgnore
    public List<Comment> getComments() { return comments; }

    @JsonIgnore
    public List<Like> getLikes() { return likes; }
}
