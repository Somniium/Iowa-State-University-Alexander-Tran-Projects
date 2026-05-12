package onetoone.Reviews;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import onetoone.Admins.AdminRepository;
import onetoone.Albums.Album;
import onetoone.Albums.AlbumRepository;
import onetoone.Posts.PostRepository;
import onetoone.Books.Book;
import onetoone.Books.BookRepository;
import onetoone.Games.Game;
import onetoone.Games.GameRepository;
import onetoone.Movies.Movie;
import onetoone.Movies.MovieRepository;
import onetoone.Notifications.NotificationService;


/**
 *
 * @author Alexander Tran and Kamil Halupka
 *
 */

@RestController
@Tag(name = "Reviews", description = "Manage reviews and the posts/media attached to them")
public class ReviewController {

    @Autowired ReviewRepository reviewRepository;
    @Autowired UserRepository   userRepository;
    @Autowired AdminRepository  adminRepository;
    @Autowired PostRepository   postRepository;
    @Autowired AlbumRepository  albumRepository;
    @Autowired BookRepository   bookRepository;
    @Autowired GameRepository   gameRepository;
    @Autowired MovieRepository  movieRepository;
    @Autowired NotificationService notificationService;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "Returns all reviews", description = "Returns all reviews as JSON objects")
    @GetMapping(path = "/reviews")
    List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }


    @Operation(summary = "Returns review by ID", description = "Returns a review using its DB ID")
    @GetMapping(path = "/reviews/{id}")
    Review getReviewById(
            @Parameter(description = "ID of review in DB", example = "1")
            @PathVariable int id) {
        return reviewRepository.findById(id);
    }

    @Operation(summary = "Returns reviews by media type", description = "Returns all reviews matching a given media type")
    @GetMapping("/reviews/type/{mediaType}")
    List<Review> getReviewsByType(
            @Parameter(description = "Media type to filter by", example = "ALBUM")
            @PathVariable String mediaType) {
        return reviewRepository.findByMediaType(mediaType.toUpperCase());
    }

    @Operation(
            summary = "Creates a review",
            description = "Adds a review to the DB. Only mediaType, title, rating, and body are accepted from the body — " +
                    "linking the review to a user/album/book/game/movie is done with the dedicated assign endpoints."
    )
    @PostMapping(path = "/reviews")
    ResponseEntity<?> createReview(
            @Parameter(description = "JSON object of review")
            @Valid @RequestBody CreateReviewRequest body) {
        if (body == null) return ResponseEntity.badRequest().body(failure);

        Review review = new Review(
                body.mediaType.trim().toUpperCase(),
                body.title.trim(),
                body.rating,
                body.body
        );
        reviewRepository.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @Operation(summary = "Updates a review", description = "Updates an existing review using its ID. " +
            "The requester must be the review's author (requesterId query param must match the review's userId).")
    @PutMapping("/reviews/{id}")
    public ResponseEntity<?> updateReview(
            @Parameter(description = "ID of review in DB", example = "1")
            @PathVariable int id,
            @Parameter(description = "ID of the user making the edit (must be the review author)", example = "42")
            @RequestParam int requesterId,
            @Parameter(description = "JSON object of review")
            @Valid @RequestBody UpdateReviewRequest request) {

        Review existing = reviewRepository.findById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review id does not exist");
        }

        // Ownership check: only the author can update.
        // If the review is unowned (legacy data) we reject the edit — caller has to assign to a user first.
        Integer ownerId = existing.getUser() != null ? existing.getUser().getId() : null;
        if (ownerId == null || ownerId != requesterId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own reviews");
        }

        if (request.mediaType != null) existing.setMediaType(request.mediaType.trim().toUpperCase());
        if (request.title != null)     existing.setTitle(request.title.trim());
        if (request.rating != null)    existing.setRating(request.rating);
        if (request.body != null)      existing.setBody(request.body);

        reviewRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @Operation(summary = "Assigns review to user",
            description = "Links an existing review to a user. The review can only be claimed by the user it's being " +
                    "assigned to (requesterId must equal userId), and once a review has an owner it cannot be reassigned.")
    @PutMapping("/reviews/{reviewId}/user/{userId}")
    ResponseEntity<?> assignReviewToUser(
            @Parameter(description = "ID of review", example = "1")
            @PathVariable int reviewId,
            @Parameter(description = "ID of user", example = "1")
            @PathVariable int userId,
            @Parameter(description = "ID of the user making the call (must equal userId)", example = "1")
            @RequestParam int requesterId) {

        // Authorization: callers can only attach reviews to themselves.
        // Without this check, anyone can attach inflammatory content to someone else's profile (IDOR).
        if (requesterId != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only assign a review to yourself");
        }

        Review review = reviewRepository.findById(reviewId);
        User user = userRepository.findByUserId(userId);

        if (review == null || user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure);
        }

        // Don't let an already-attached review be reassigned (would let a previous owner steal a review).
        if (review.getUser() != null && review.getUser().getId() != userId) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Review is already assigned to another user");
        }

        review.setUser(user);
        reviewRepository.save(review);
        notificationService.notify(user, "REVIEW",
                "Your review of \"" + review.getTitle() + "\" has been posted!");
        return ResponseEntity.ok(success);
    }

    @Operation(summary = "Assigns review to album", description = "Links an existing review to an album. Caller must be the review author.")
    @PutMapping("/reviews/{reviewId}/album/{albumId}")
    ResponseEntity<?> assignReviewToAlbum(
            @Parameter(description = "ID of review", example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "ID of album", example = "1")
            @PathVariable long albumId,
            @Parameter(description = "ID of the user making the call (must be review author)", example = "42")
            @RequestParam int requesterId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        Album album = albumRepository.findByAlbumId(albumId);

        if (review == null || album == null) return new ResponseEntity<>("Error: Album not found.", HttpStatus.NOT_FOUND);

        ResponseEntity<?> denied = denyIfNotOwner(review, requesterId);
        if (denied != null) return denied;

        review.setAlbum(album);
        reviewRepository.save(review);
        return new ResponseEntity<>("Successfully added review to album!", HttpStatus.OK);
    }

    @Operation(summary = "Purge orphan reviews",
            description = "Admin-only. Deletes any posts wrapping ownerless reviews, then deletes the ownerless reviews themselves. " +
                    "Used to clean up data left behind by older user-deletion logic that didn't cascade to reviews.")
    @Transactional
    @DeleteMapping("/reviews/orphans")
    ResponseEntity<?> purgeOrphanReviews(
            @Parameter(description = "ID of the admin user making the call", example = "1")
            @org.springframework.web.bind.annotation.RequestParam int requesterId) {
        User requester = userRepository.findByUserId(requesterId);
        if (requester == null || adminRepository.findByUser(requester) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin only");
        }
        long postsRemoved = postRepository.deleteByReview_UserIsNull();
        long reviewsRemoved = reviewRepository.deleteByUserIsNull();
        return ResponseEntity.ok("{\"postsRemoved\":" + postsRemoved + ",\"reviewsRemoved\":" + reviewsRemoved + "}");
    }

    @Operation(summary = "Deletes a review", description = "Deletes a review from the DB. Caller must be the review's author.")
    @DeleteMapping(path = "/reviews/{id}")
    ResponseEntity<?> deleteReview(
            @Parameter(description = "ID of review in DB", example = "1")
            @PathVariable int id,
            @Parameter(description = "ID of the user deleting (must be review author)", example = "42")
            @RequestParam int requesterId) {
        Review review = reviewRepository.findById(id);
        if (review == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");
        }
        ResponseEntity<?> denied = denyIfNotOwner(review, requesterId);
        if (denied != null) return denied;

        reviewRepository.deleteById(id);
        return ResponseEntity.ok(success);
    }

    @Operation(
            summary = "Assign a review to a book",
            parameters = {
                    @Parameter(name = "reviewId", required = true, example = "1"),
                    @Parameter(name = "bookId",   required = true, example = "7"),
                    @Parameter(name = "requesterId", required = true, example = "42",
                            description = "User performing the action — must be the review's author")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Assigned"),
                    @ApiResponse(responseCode = "403", description = "Not the review's author", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/reviews/{reviewId}/book/{bookId}")
    ResponseEntity<?> assignReviewToBook(@PathVariable int reviewId,
                                         @PathVariable Long bookId,
                                         @RequestParam int requesterId) {
        Review review = reviewRepository.findById(reviewId);
        Book book = bookRepository.findById(bookId).orElse(null);
        if (review == null || book == null)
            return new ResponseEntity<>("Error: Book not found.", HttpStatus.NOT_FOUND);

        ResponseEntity<?> denied = denyIfNotOwner(review, requesterId);
        if (denied != null) return denied;

        review.setBook(book);
        reviewRepository.save(review);
        return new ResponseEntity<>("Successfully added review to book.", HttpStatus.OK);
    }

    @Operation(
            summary = "Assign a review to a game",
            parameters = {
                    @Parameter(name = "reviewId", required = true, example = "1"),
                    @Parameter(name = "gameId",   required = true, example = "2"),
                    @Parameter(name = "requesterId", required = true, example = "42",
                            description = "User performing the action — must be the review's author")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Assigned"),
                    @ApiResponse(responseCode = "403", description = "Not the review's author", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/reviews/{reviewId}/game/{gameId}")
    ResponseEntity<?> assignReviewToGame(@PathVariable int reviewId,
                                         @PathVariable Long gameId,
                                         @RequestParam int requesterId) {
        Review review = reviewRepository.findById(reviewId);
        Game game = gameRepository.findById(gameId).orElse(null);
        if (review == null || game == null)
            return new ResponseEntity<>("Error: Game not found.", HttpStatus.NOT_FOUND);

        ResponseEntity<?> denied = denyIfNotOwner(review, requesterId);
        if (denied != null) return denied;

        review.setGame(game);
        reviewRepository.save(review);
        return new ResponseEntity<>("Successfully added review to game.", HttpStatus.OK);
    }

    @Operation(
            summary = "Assign a review to a movie or show",
            parameters = {
                    @Parameter(name = "reviewId", required = true, example = "1"),
                    @Parameter(name = "movieId",  required = true, example = "5"),
                    @Parameter(name = "requesterId", required = true, example = "42",
                            description = "User performing the action — must be the review's author")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Assigned"),
                    @ApiResponse(responseCode = "403", description = "Not the review's author", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/reviews/{reviewId}/movie/{movieId}")
    ResponseEntity<?> assignReviewToMovie(@PathVariable int reviewId,
                                          @PathVariable Long movieId,
                                          @RequestParam int requesterId) {
        Review review = reviewRepository.findById(reviewId);
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (review == null || movie == null)
            return new ResponseEntity<>("Error: Movie/Show not found.", HttpStatus.NOT_FOUND);

        ResponseEntity<?> denied = denyIfNotOwner(review, requesterId);
        if (denied != null) return denied;

        review.setMovie(movie);
        reviewRepository.save(review);
        return new ResponseEntity<>("Successfully added review to movie/show.", HttpStatus.OK);
    }

    /**
     * Returns null if requesterId is the review's author, otherwise a 403 ResponseEntity.
     * Reviews without an owner cannot be modified via assign-media endpoints — call
     * /reviews/{id}/user/{userId} first to claim the review.
     */
    private ResponseEntity<?> denyIfNotOwner(Review review, int requesterId) {
        Integer ownerId = review.getUser() != null ? review.getUser().getId() : null;
        if (ownerId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Review has no owner — assign it to a user first via PUT /reviews/{id}/user/{userId}");
        }
        if (ownerId != requesterId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only modify your own reviews");
        }
        return null;
    }

    /**
     * Whitelisted DTO for review creation. Stops mass-assignment via raw `@RequestBody Review`
     * (which previously let callers set id, user, album, book, game, movie directly).
     */
    @Schema(description = "Request body for creating a review")
    static class CreateReviewRequest {
        @NotBlank
        @Pattern(regexp = "(?i)ALBUM|BOOK|GAME|MOVIE|SHOW",
                message = "mediaType must be one of: ALBUM, BOOK, GAME, MOVIE, SHOW")
        @Schema(description = "Media category", example = "MOVIE")
        public String mediaType;

        @NotBlank
        @Size(max = 200)
        @Schema(description = "Title of the reviewed item", example = "Inception")
        public String title;

        @Min(0) @Max(100)
        @Schema(description = "Metacritic-style score from 0 to 100", example = "92")
        public int rating;

        @Size(max = 2000)
        @Schema(description = "Full review text", example = "A mind-bending masterpiece.")
        public String body;
    }

    /** Whitelisted DTO for review updates — all fields optional, but bounded. */
    @Schema(description = "Request body for updating a review")
    static class UpdateReviewRequest {
        @Pattern(regexp = "(?i)ALBUM|BOOK|GAME|MOVIE|SHOW",
                message = "mediaType must be one of: ALBUM, BOOK, GAME, MOVIE, SHOW")
        public String mediaType;

        @Size(max = 200)
        public String title;

        @Min(0) @Max(100)
        public Integer rating;

        @Size(max = 2000)
        public String body;
    }
}
