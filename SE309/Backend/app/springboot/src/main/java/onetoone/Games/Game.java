package onetoone.Games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Reviews.Review;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Schema(description = "A video game fetched and stored from the RAWG API")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Internal DB ID", example = "1")
    private Long id;

    @Column(unique = true)
    @Schema(description = "RAWG game ID (used as the external unique key)", example = "3498")
    private String rawgId;

    @Schema(description = "Game title", example = "Grand Theft Auto V")
    private String title;

    @Schema(description = "Primary genre", example = "Action")
    private String genre;

    @Schema(description = "Original release date", example = "2013-09-17")
    private String releaseDate;

    @Schema(description = "Developer / publisher name", example = "Rockstar Games")
    private String developer;

    @Column(length = 2000)
    @Schema(description = "Short description / summary of the game")
    private String description;

    @Schema(description = "URL to the game's cover/background image")
    private String coverUrl;

    @Schema(description = "RAWG metacritic score (0–100)", example = "97")
    private Integer metacriticScore;

    @Schema(description = "Computed average rating from CyVal reviews (1–5)", example = "4")
    private int rating = 0;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Game() {}

    public Game(String rawgId, String title, String genre, String releaseDate,
                String developer, String description, String coverUrl, Integer metacriticScore) {
        this.rawgId = rawgId;
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.developer = developer;
        this.description = description;
        this.coverUrl = coverUrl;
        this.metacriticScore = metacriticScore;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        review.setGame(this);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getRawgId() { return rawgId; }
    public void setRawgId(String rawgId) { this.rawgId = rawgId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getDeveloper() { return developer; }
    public void setDeveloper(String developer) { this.developer = developer; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getMetacriticScore() { return metacriticScore; }
    public void setMetacriticScore(Integer metacriticScore) { this.metacriticScore = metacriticScore; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
