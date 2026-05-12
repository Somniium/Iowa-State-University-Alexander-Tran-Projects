package onetoone.Movies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Reviews.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents either a Movie OR a TV Show.
 * mediaType values: MOVIE | SHOW
 */
@Entity
@Table(name = "movies")
@Schema(description = "A movie or TV show fetched from the TMDB API. mediaType is MOVIE or SHOW.")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Internal DB ID", example = "1")
    @Column(name = "movie_id")
    private Long id;

    @Column(unique = true)
    @Schema(description = "TMDB ID combined with type, e.g. 'movie-550' or 'tv-1396'", example = "movie-550")
    private String tmdbId;

    @Schema(description = "MOVIE or SHOW", example = "MOVIE")
    private String mediaType;

    @Schema(description = "Title of the movie or show", example = "Fight Club")
    private String title;

    @Schema(description = "Primary genre", example = "Drama")
    private String genre;

    @Schema(description = "Release date (movies) or first air date (shows)", example = "1999-10-15")
    private String releaseDate;

    @Schema(description = "Director (movies) or creator (shows)", example = "David Fincher")
    private String director;

    @Column(length = 2000)
    @Schema(description = "Plot overview / synopsis")
    private String overview;

    @Schema(description = "URL to the TMDB poster image")
    private String posterUrl;

    @Schema(description = "TMDB audience vote average (0–10)", example = "8.4")
    private Double tmdbScore;

    @Schema(description = "Computed average rating from CyVal reviews (1–5)", example = "4")
    private int rating = 0;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Movie() {}

    public Movie(String tmdbId, String mediaType, String title, String genre, String releaseDate,
                 String director, String overview, String posterUrl, Double tmdbScore) {
        this.tmdbId = tmdbId;
        this.mediaType = mediaType;
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.director = director;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.tmdbScore = tmdbScore;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        review.setMovie(this);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getTmdbId() { return tmdbId; }
    public void setTmdbId(String tmdbId) { this.tmdbId = tmdbId; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public Double getTmdbScore() { return tmdbScore; }
    public void setTmdbScore(Double tmdbScore) { this.tmdbScore = tmdbScore; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
