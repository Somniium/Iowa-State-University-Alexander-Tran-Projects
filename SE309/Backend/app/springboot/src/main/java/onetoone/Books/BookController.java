package onetoone.Books;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Reviews.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Book endpoints — backed by the Hardcover GraphQL API.
 *
 * Typical flow:
 *   1. GET  /search-books?q=dune             → browse Hardcover results (not saved)
 *   2. POST /books/save?hardcoverId=82563    → save to DB by Hardcover ID
 *   3. PUT  /reviews/{id}/book/{bookId}      → link a review (in ReviewController)
 */
@RestController
@Tag(name = "Books", description = "Search the Hardcover API and manage saved books.")
public class BookController {

    @Autowired private BookRepository bookRepository;
    @Autowired private HardcoverService hardcoverService;

    @Operation(
            summary = "Search Hardcover for books",
            description = "Queries the Hardcover API by title and returns results WITHOUT saving them to DB.",
            parameters = {
                    @Parameter(name = "q",   description = "Book title to search for", required = true, example = "Dune"),
                    @Parameter(name = "max", description = "Max results (default 5)", example = "5")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Matching books from Hardcover",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Book.class))))
            }
    )
    @GetMapping("/search-books")
    public ResponseEntity<?> searchBooks(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "max", defaultValue = "5") int max) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Search query is required"));
        }

        try {
            return ResponseEntity.ok(hardcoverService.searchBooks(query, max));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "message", "Hardcover search failed",
                            "details", e.getMessage()
                    ));
        }
    }

    @Operation(
            summary = "Save a book to the database by Hardcover ID",
            description = "Fetches full details from Hardcover and saves to DB. Returns the existing record if already saved. Get the hardcoverId from /search-books.",
            parameters = {
                    @Parameter(name = "hardcoverId", description = "Hardcover numeric book ID", required = true, example = "82563")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Saved (or already existing) book",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                    @ApiResponse(responseCode = "500", description = "Hardcover API error", content = @Content)
            }
    )
    @PostMapping("/books/save")
    public ResponseEntity<?> saveBook(@RequestParam String hardcoverId) {
        try {
            Book existing = bookRepository.findByVolumeId(hardcoverId).orElse(null);
            if (existing != null) return ResponseEntity.ok(existing);

            Book fetched = hardcoverService.getBookById(hardcoverId);
            bookRepository.save(fetched);
            return ResponseEntity.ok(fetched);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hardcover fetch failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get all saved books",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All books in DB",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Book.class))))
            }
    )
    @GetMapping("/books")
    public List<Book> getAllBooks() { return bookRepository.findAll(); }

    @Operation(
            summary = "Get a saved book by DB ID",
            parameters = { @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Book found",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(
            summary = "Get a saved book by title",
            parameters = { @Parameter(name = "title", description = "Exact book title", required = true, example = "Dune") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Book found",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping("/books/name")
    public ResponseEntity<?> getBookByTitle(@RequestParam String title) {
        Book book = bookRepository.findByTitle(title);
        if (book == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        return ResponseEntity.ok(book);
    }

    @Operation(
            summary = "Get all reviews for a book",
            parameters = { @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of reviews"),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping("/books/{id}/reviews")
    public ResponseEntity<?> getBookReviews(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        return ResponseEntity.ok(book.getReviews());
    }

    @Operation(
            summary = "Recalculate and update average rating for a book",
            description = "Averages all linked review ratings and saves the result.",
            parameters = { @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rating updated"),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/books/{id}/update-rating")
    public ResponseEntity<?> updateRating(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        List<Review> reviews = book.getReviews();
        if (reviews.isEmpty()) return ResponseEntity.ok("No reviews yet — rating unchanged");
        int avg = reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
        book.setRating(avg);
        bookRepository.save(book);
        return ResponseEntity.ok("Rating updated to " + avg);
    }

    @Operation(
            summary = "Update book details manually",
            parameters = { @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated book",
                            content = @Content(schema = @Schema(implementation = Book.class))),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book updated) {
        Book existing = bookRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        existing.setTitle(updated.getTitle());
        existing.setAuthors(updated.getAuthors());
        existing.setPublishedDate(updated.getPublishedDate());
        existing.setDescription(updated.getDescription());
        existing.setThumbnailUrl(updated.getThumbnailUrl());
        existing.setIsbn13(updated.getIsbn13());
        bookRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @Operation(
            summary = "Delete a book",
            parameters = { @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        bookRepository.deleteById(id);
        return ResponseEntity.ok("Deleted book id=" + id);
    }
}
