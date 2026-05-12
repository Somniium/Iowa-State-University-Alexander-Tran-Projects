package onetoone.Likes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Posts.Post;
import onetoone.Users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * A Like is cast by a User on a Post.
 * Likes attach to the Post
 * Unique constraint prevents a user from liking the same post twice.
 */
@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@Schema(description = "A like/upvote cast by a User on a Post")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique like ID", example = "7")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    @Schema(description = "Timestamp when the like was created", example = "2024-11-01T14:30:00")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Like() {}

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Schema(description = "ID of the user who liked", example = "42")
    public int getUserId() { return user != null ? user.getId() : 0; }

    @Schema(description = "Name of the user who liked", example = "John Doe")
    public String getUserName() { return user != null ? user.getName() : null; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    @Schema(description = "ID of the post that was liked", example = "3")
    public int getPostId() { return post != null ? post.getId() : 0; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
