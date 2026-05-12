package onetoone.Comments;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Admins.AdminRepository;
import onetoone.Notifications.NotificationService;
import onetoone.Posts.Post;
import onetoone.Posts.PostRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Comments belong to Posts, not Reviews directly.
 */
@RestController
@Tag(name = "Comments", description = "Post, edit, delete, and list comments on Posts")
public class CommentController {

    @Autowired private CommentRepository  commentRepository;
    @Autowired private PostRepository     postRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private AdminRepository    adminRepository;
    @Autowired private NotificationService notificationService;

    @Operation(
            summary = "Add a comment to a post",
            description = "Creates a new comment on the given post. Fires a live WebSocket notification to the post author.",
            parameters = {
                    @Parameter(name = "postId",   description = "ID of the post to comment on",    required = true, example = "1"),
                    @Parameter(name = "authorId", description = "ID of the user posting the comment", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comment created",
                            content = @Content(schema = @Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "400", description = "Empty body", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Post or user not found", content = @Content)
            }
    )
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable int postId,
            @RequestParam int authorId,
            @RequestBody CommentRequest request) {

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");

        User author = userRepository.findByUserId(authorId);
        if (author == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if (request.body == null || request.body.isBlank())
            return ResponseEntity.badRequest().body("Comment body cannot be empty");

        Comment comment = new Comment(request.body.trim(), author, post);
        commentRepository.save(comment);

        // Notify post author (unless they're commenting on their own post)
        User postAuthor = post.getAuthor();
        if (postAuthor != null && postAuthor.getId() != author.getId()) {
            String mediaTitle = post.getMediaTitle() != null ? post.getMediaTitle() : "your post";
            notificationService.notify(postAuthor, "COMMENT",
                    author.getName() + " commented on your post about \"" + mediaTitle + "\"");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @Operation(
            summary = "Get all comments on a post",
            description = "Returns comments in chronological order (oldest first).",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of comments",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Comment.class)))),
                    @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
            }
    )
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable int postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        return ResponseEntity.ok(commentRepository.findByPost_IdOrderByCreatedAtAsc(postId));
    }

    @Operation(
            summary = "Get comment count for a post",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON count",
                            content = @Content(schema = @Schema(example = "{\"count\":5}")))
            }
    )
    @GetMapping("/posts/{postId}/comments/count")
    public ResponseEntity<?> getCommentCount(@PathVariable int postId) {
        long count = commentRepository.countByPost_Id(postId);
        return ResponseEntity.ok("{\"count\":" + count + "}");
    }

    @Operation(
            summary = "Get all comments posted by a user",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of comments",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Comment.class)))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<?> getUserComments(@PathVariable int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.ok(commentRepository.findByAuthor_IdOrderByCreatedAtDesc(userId));
    }

    @Operation(
            summary = "Get a single comment by ID",
            parameters = {
                    @Parameter(name = "id", description = "Comment ID", required = true, example = "15")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "The comment",
                            content = @Content(schema = @Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
            }
    )
    @GetMapping("/comments/{id}")
    public ResponseEntity<?> getComment(@PathVariable int id) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        return ResponseEntity.ok(comment);
    }

    @Operation(
            summary = "Edit a comment",
            description = "Only the original author may edit their comment.",
            parameters = {
                    @Parameter(name = "id",       description = "Comment ID",                              required = true, example = "15"),
                    @Parameter(name = "authorId", description = "ID of the user editing (must be author)", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated comment",
                            content = @Content(schema = @Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "403", description = "Not the author", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
            }
    )
    @PutMapping("/comments/{id}")
    public ResponseEntity<?> editComment(
            @PathVariable int id,
            @RequestParam int authorId,
            @RequestBody CommentRequest request) {

        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
        if (!Integer.valueOf(authorId).equals(comment.getAuthorId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own comments");
        if (request.body == null || request.body.isBlank())
            return ResponseEntity.badRequest().body("Comment body cannot be empty");

        comment.setBody(request.body.trim());
        commentRepository.save(comment);
        return ResponseEntity.ok(comment);
    }

    @Operation(
            summary = "Delete a comment",
            description = "Can be deleted by the comment author, the post author, or any admin.",
            parameters = {
                    @Parameter(name = "id",          description = "Comment ID",                    required = true, example = "15"),
                    @Parameter(name = "requesterId", description = "ID of the user requesting deletion", required = true, example = "42")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted",
                            content = @Content(schema = @Schema(example = "{\"message\":\"success\"}"))),
                    @ApiResponse(responseCode = "403", description = "Not authorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
            }
    )
    @Transactional
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable int id,
            @RequestParam int requesterId) {

        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");

        boolean isAuthor     = Integer.valueOf(requesterId).equals(comment.getAuthorId());
        boolean isPostAuthor = comment.getPost() != null
                && comment.getPost().getAuthorId() != null
                && comment.getPost().getAuthorId().equals(requesterId);
        User requester = userRepository.findByUserId(requesterId);
        boolean isAdmin = requester != null && adminRepository.findByUser(requester) != null;

        if (!isAuthor && !isPostAuthor && !isAdmin)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this comment");

        Post commentPost = comment.getPost();
        if (commentPost != null) {
            commentPost.getComments().remove(comment);
            comment.setPost(null);
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok("{\"message\":\"success\"}");
    }

    @Schema(description = "Request body for creating or editing a comment")
    static class CommentRequest {
        @Schema(description = "The text content of the comment", example = "Totally agree, this game is a masterpiece.", required = true)
        public String body;
    }
}
