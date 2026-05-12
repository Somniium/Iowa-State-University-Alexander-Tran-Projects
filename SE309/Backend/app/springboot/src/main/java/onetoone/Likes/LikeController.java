package onetoone.Likes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Notifications.NotificationService;
import onetoone.Posts.Post;
import onetoone.Posts.PostRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Likes", description = "Like/upvote posts. POST toggles the like on and off.")
public class LikeController {

    @Autowired private LikeRepository     likeRepository;
    @Autowired private PostRepository     postRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private NotificationService notificationService;

    @Operation(
            summary = "Toggle like on a post",
            description = "Liked → unliked, unliked → liked. Fires a WebSocket notification to the post author on a new like.",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post", required = true, example = "1"),
                    @Parameter(name = "userId", description = "ID of the user liking/unliking", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Like added",
                            content = @Content(schema = @Schema(example = "{\"liked\":true,\"count\":12}"))),
                    @ApiResponse(responseCode = "200", description = "Like removed (toggled off)",
                            content = @Content(schema = @Schema(example = "{\"liked\":false,\"count\":11}"))),
                    @ApiResponse(responseCode = "404", description = "Post or user not found", content = @Content)
            }
    )
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<?> toggleLike(
            @PathVariable int postId,
            @RequestParam int userId) {

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");

        User user = userRepository.findByUserId(userId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if (likeRepository.existsByUser_IdAndPost_Id(userId, postId)) {
            likeRepository.deleteByUser_IdAndPost_Id(userId, postId);
            long count = likeRepository.countByPost_Id(postId);
            return ResponseEntity.ok("{\"liked\":false,\"count\":" + count + "}");
        }

        likeRepository.save(new Like(user, post));
        long count = likeRepository.countByPost_Id(postId);

        // Notify the post author (unless they liked their own post)
        User postAuthor = post.getAuthor();
        if (postAuthor != null && postAuthor.getId() != userId) {
            String mediaTitle = post.getMediaTitle() != null ? post.getMediaTitle() : "your post";
            notificationService.notify(postAuthor, "LIKE",
                    user.getName() + " liked your post about \"" + mediaTitle + "\"");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"liked\":true,\"count\":" + count + "}");
    }

    @Operation(
            summary = "Unlike a post (explicit)",
            description = "Explicitly removes a like. Returns 404 if the user had not liked the post.",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post", required = true, example = "1"),
                    @Parameter(name = "userId", description = "ID of the user", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unliked",
                            content = @Content(schema = @Schema(example = "{\"liked\":false,\"count\":11}"))),
                    @ApiResponse(responseCode = "404", description = "Like not found", content = @Content)
            }
    )
    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<?> unlike(
            @PathVariable int postId,
            @RequestParam int userId) {

        // Single DELETE — its return value tells us whether anything matched, so we
        // skip the previous existsBy + deleteBy two-query dance. Also fixes a stale-count
        // bug: see deleteByUser_IdAndPost_Id docs for why @Modifying flush+clear matters here.
        int deleted = likeRepository.deleteByUser_IdAndPost_Id(userId, postId);
        if (deleted == 0)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Like not found");

        long count = likeRepository.countByPost_Id(postId);
        return ResponseEntity.ok("{\"liked\":false,\"count\":" + count + "}");
    }

    @Operation(
            summary = "Get total like count for a post",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON count",
                            content = @Content(schema = @Schema(example = "{\"count\":42}")))
            }
    )
    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<?> getLikeCount(@PathVariable int postId) {
        long count = likeRepository.countByPost_Id(postId);
        return ResponseEntity.ok("{\"count\":" + count + "}");
    }

    @Operation(
            summary = "List all likes on a post",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of likes",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Like.class)))),
                    @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
            }
    )
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<?> getLikes(@PathVariable int postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        return ResponseEntity.ok(likeRepository.findByPost_Id(postId));
    }

    @Operation(
            summary = "Check if a user has liked a post",
            description = "Returns the like status for this user plus the current total count.",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post",           required = true, example = "1"),
                    @Parameter(name = "userId", description = "ID of the user to check",  required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status and count",
                            content = @Content(schema = @Schema(example = "{\"liked\":true,\"count\":12}")))
            }
    )
    @GetMapping("/posts/{postId}/likes/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable int postId,
            @RequestParam int userId) {
        boolean liked = likeRepository.existsByUser_IdAndPost_Id(userId, postId);
        long count    = likeRepository.countByPost_Id(postId);
        return ResponseEntity.ok("{\"liked\":" + liked + ",\"count\":" + count + "}");
    }

    @Operation(
            summary = "Get all posts a user has liked",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of Like objects",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Like.class)))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/likes")
    public ResponseEntity<?> getUserLikes(@PathVariable int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.ok(likeRepository.findByUser_Id(userId));
    }
}
