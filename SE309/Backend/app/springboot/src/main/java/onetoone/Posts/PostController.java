package onetoone.Posts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Admins.AdminRepository;
import onetoone.Notifications.NotificationService;
import onetoone.Reviews.Review;
import onetoone.Reviews.ReviewRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PostController — the social/feed layer.
 *
 * Typical flow:
 *   1. User creates a Review (POST /reviews)
 *   2. User links the Review to their account (PUT /reviews/{id}/user/{userId})
 *   3. User links the Review to a media item (PUT /reviews/{id}/game/{gameId} etc.)
 *   4. User publishes it as a Post (POST /reviews/{reviewId}/publish?authorId=)
 *   5. Other users see it in the feed (GET /feed)
 *   6. Other users comment (POST /posts/{id}/comments) and like (POST /posts/{id}/likes)
 */
@RestController
@Tag(name = "Posts", description = "Publish reviews as posts to the social feed. Comments and likes attach to posts.")
public class PostController {

    @Autowired private PostRepository    postRepository;
    @Autowired private ReviewRepository  reviewRepository;
    @Autowired private UserRepository    userRepository;
    @Autowired private AdminRepository   adminRepository;
    @Autowired private NotificationService notificationService;

    // PUBLISH

    @Operation(
            summary = "Publish a review as a post",
            description = """
            Wraps an existing Review in a Post and adds it to the public feed.
            A review can only be published once — attempting to publish the same review twice returns 409.
            Fires a REVIEW notification to the author confirming the post went live.
            """,
            parameters = {
                    @Parameter(name = "reviewId", description = "ID of the Review to publish", required = true, example = "3"),
                    @Parameter(name = "authorId", description = "ID of the User publishing the post", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Post created and live on the feed",
                            content = @Content(schema = @Schema(implementation = Post.class))),
                    @ApiResponse(responseCode = "404", description = "Review or user not found", content = @Content),
                    @ApiResponse(responseCode = "409", description = "This review has already been published", content = @Content)
            }
    )
    @PostMapping("/reviews/{reviewId}/publish")
    public ResponseEntity<?> publishReview(
            @PathVariable int reviewId,
            @RequestParam int authorId,
            @RequestBody(required = false) PublishRequest body) {

        Review review = reviewRepository.findById(reviewId);
        if (review == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");

        User author = userRepository.findByUserId(authorId);
        if (author == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if (postRepository.existsByReview_Id(reviewId))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This review has already been published as a post");

        String caption    = body != null ? body.caption    : null;
        String visibility = body != null && body.visibility != null ? body.visibility : "PUBLIC";

        Post post = new Post(review, author, caption, visibility);
        postRepository.save(post);

        notificationService.notify(author, "REVIEW",
                "Your review of \"" + review.getTitle() + "\" is now live on the feed!");

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PostMapping(value = "/reviews/{reviewId}/publish", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> publishReviewFromForm(
            @PathVariable int reviewId,
            @RequestParam int authorId,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) String visibility) {

        PublishRequest body = new PublishRequest();
        body.caption = caption;
        body.visibility = visibility;
        return publishReview(reviewId, authorId, body);
    }

    // FEED

    @Operation(
            summary = "Get the public feed",
            description = "Returns all PUBLIC posts sorted newest first. This is the main app feed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of posts",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Post.class))))
            }
    )
    @GetMapping("/feed")
    public List<Post> getFeed() {
        return postRepository.findByVisibilityOrderByPublishedAtDesc("PUBLIC");
    }

    @Operation(
            summary = "Get feed filtered by media type",
            description = "Returns PUBLIC posts for a specific media type (ALBUM, BOOK, GAME, MOVIE, SHOW) newest first.",
            parameters = {
                    @Parameter(name = "mediaType", description = "Media type to filter by", required = true, example = "GAME")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Filtered list of posts",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Post.class))))
            }
    )
    @GetMapping("/feed/type/{mediaType}")
    public List<Post> getFeedByType(@PathVariable String mediaType) {
        return postRepository.findByReview_MediaTypeOrderByPublishedAtDesc(mediaType.toUpperCase());
    }

    @Operation(
            summary = "Get all posts by a user",
            description = "Returns all posts published by a specific user, newest first.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of posts",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Post.class)))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<?> getUserPosts(@PathVariable int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.ok(postRepository.findByAuthor_IdOrderByPublishedAtDesc(userId));
    }

    // ---------------------------------------------------------------- GET ONE

    @Operation(
            summary = "Get a single post by ID",
            parameters = {
                    @Parameter(name = "id", description = "Post ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "The post",
                            content = @Content(schema = @Schema(implementation = Post.class))),
                    @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
            }
    )
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(@PathVariable int id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        return ResponseEntity.ok(post);
    }

    // UPDATE

    @Operation(
            summary = "Edit a post's caption or visibility",
            description = "Only the caption and visibility can be edited after publishing. The underlying review is not changed.",
            parameters = {
                    @Parameter(name = "id",        description = "Post ID",                         required = true, example = "1"),
                    @Parameter(name = "requesterId", description = "ID of the user making the edit (must be author)", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated post",
                            content = @Content(schema = @Schema(implementation = Post.class))),
                    @ApiResponse(responseCode = "403", description = "Not the author", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
            }
    )
    @PutMapping("/posts/{id}")
    public ResponseEntity<?> editPost(
            @PathVariable int id,
            @RequestParam int requesterId,
            @RequestBody PublishRequest body) {

        Post post = postRepository.findById(id).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");

        if (!Integer.valueOf(requesterId).equals(post.getAuthorId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own posts");

        if (body.caption != null)    post.setCaption(body.caption);
        if (body.visibility != null) post.setVisibility(body.visibility.toUpperCase());

        postRepository.save(post);
        return ResponseEntity.ok(post);
    }

    // DELETE

    @Operation(
            summary = "Delete a post",
            description = "Deletes the post and all its comments and likes (cascade). The underlying Review is NOT deleted. Author or admin only.",
            parameters = {
                    @Parameter(name = "id",          description = "Post ID",                               required = true, example = "1"),
                    @Parameter(name = "requesterId", description = "ID of the user deleting (must be author)", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted",
                            content = @Content(schema = @Schema(example = "{\"message\":\"success\"}"))),
                    @ApiResponse(responseCode = "403", description = "Not the author", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
            }
    )
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable int id,
            @RequestParam int requesterId) {

        Post post = postRepository.findById(id).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");

        boolean isAuthor = Integer.valueOf(requesterId).equals(post.getAuthorId());
        User requester = userRepository.findByUserId(requesterId);
        boolean isAdmin = requester != null && adminRepository.findByUser(requester) != null;

        if (!isAuthor && !isAdmin)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this post");

        postRepository.deleteById(id);
        return ResponseEntity.ok("{\"message\":\"success\"}");
    }

    // DTO

    @Schema(description = "Request body for publishing or editing a post")
    static class PublishRequest {
        @Schema(description = "Optional caption to display above the review", example = "Finally finished this one!")
        public String caption;
        @Schema(description = "Visibility: PUBLIC or FRIENDS (default PUBLIC)", example = "PUBLIC")
        public String visibility;
    }
}
