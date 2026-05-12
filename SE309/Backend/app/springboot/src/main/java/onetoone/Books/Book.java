package onetoone.Books;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Reviews.Review;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Schema(description = "A book fetched from the Hardcover API")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Internal DB ID", example = "1")
    private Long id;

    @Column(unique = true)
    @Schema(description = "Hardcover volume/edition ID used as the external unique key", example = "42")
    private String volumeId;

    @Schema(description = "Book title", example = "Harry Potter and the Sorcerer's Stone")
    private String title;

    @Schema(description = "Comma-separated list of authors", example = "J. K. Rowling")
    private String authors;

    @Schema(description = "Year or date of first publication", example = "1997")
    private String publishedDate;

    @Column(length = 2000)
    @Schema(description = "Plot summary or description")
    private String description;

    @Schema(description = "URL to the cover image")
    private String thumbnailUrl;

    @Schema(description = "ISBN-13", example = "9780590353427")
    private String isbn13;

    @Schema(description = "Computed average rating from CyVal reviews (1–5)", example = "4")
    private int rating = 0;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Book() {}

    public void addReview(Review review) {
        this.reviews.add(review);
        review.setBook(this);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getVolumeId() { return volumeId; }
    public void setVolumeId(String volumeId) { this.volumeId = volumeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
