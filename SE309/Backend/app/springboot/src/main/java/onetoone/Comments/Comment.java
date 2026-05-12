package onetoone.Comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Posts.Post;
import onetoone.Users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * A Comment is written by a User on a Post.
 * Comments attach to the Post (the social object), not the raw Review data.
 */
@Entity
@Table(name = "comments")
@Schema(description = "A comment written by a User on a Post")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique comment ID", example = "15")
    private int id;

    @Column(length = 2000, nullable = false)
    @Schema(description = "Text content of the comment", example = "Great review! I totally agree about the bass line.")
    private String body;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post;

    // Read-only mirror of the post_id FK column. We mirror the FK directly so that
    // serialization doesn't depend on the Post relationship being initialized — getPostId()
    // was returning 0 in some responses because post.getId() was hitting an uninitialized
    // proxy/primitive default.
    @Column(name = "post_id", insertable = false, updatable = false)
    @JsonIgnore
    private Integer postIdFk;

    @Schema(description = "When the comment was first created", example = "2024-11-01T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the comment was last edited", example = "2024-11-02T09:15:00")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { this.createdAt = this.updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Comment() {}

    public Comment(String body, User author, Post post) {
        this.body = body;
        this.author = author;
        this.post = post;
    }

    public int getId() { return id; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    @Schema(description = "ID of the comment author", example = "42")
    public int getAuthorId() { return author != null ? author.getId() : 0; }

    @Schema(description = "Display name of the comment author", example = "Alex Tran")
    public String getAuthorName() { return author != null ? author.getName() : null; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    @Schema(description = "ID of the post this comment belongs to", example = "3")
    public int getPostId() {
        if (postIdFk != null) return postIdFk;
        return post != null ? post.getId() : 0;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
