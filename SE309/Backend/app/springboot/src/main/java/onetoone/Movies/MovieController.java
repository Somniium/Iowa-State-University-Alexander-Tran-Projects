package onetoone.Movies;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Movie and TV Show endpoints — backed by the TMDB API.
 *
 * Typical flow:
 *   1. GET /search-movies?q=inception      → browse TMDB results (not saved)
 *   2. GET /search-shows?q=breaking+bad    → browse TMDB TV results (not saved)
 *   3. POST /movies/save?tmdbId=27205&type=movie  → save to DB
 *   4. PUT /reviews/{id}/movie/{movieId}   → link a review  (in ReviewController)
 */
@RestController
@Tag(name = "Movies & Shows", description = "Search TMDB for movies and TV shows, save them, and manage reviews.")
public class MovieController {

    @Autowired private MovieRepository movieRepository;
    @Autowired private TMDbService tmdbService;

    // ---------------------------------------------------------------- SEARCH

    @Operation(
            summary = "Search TMDB for movies",
            description = "Returns matching movies from TMDB WITHOUT saving them. Use to let the user pick before saving.",
            parameters = {
                    @Parameter(name = "q",   description = "Movie title to search for", required = true, example = "Inception"),
                    @Parameter(name = "max", description = "Max results (default 5)", example = "5")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of matching movies",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Movie.class))))
            }
    )
    @GetMapping("/search-movies")
    public List<Movie> searchMovies(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "max", defaultValue = "5") int max) {
        return tmdbService.searchMovies(query, max);
    }

    @Operation(
            summary = "Search TMDB for TV shows",
            description = "Returns matching TV shows from TMDB WITHOUT saving them.",
            parameters = {
                    @Parameter(name = "q",   description = "Show title to search for", required = true, example = "Breaking Bad"),
                    @Parameter(name = "max", description = "Max results (default 5)", example = "5")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of matching shows",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Movie.class))))
            }
    )
    @GetMapping("/search-shows")
    public List<Movie> searchShows(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "max", defaultValue = "5") int max) {
        return tmdbService.searchShows(query, max);
    }

    // ---------------------------------------------------------------- SAVE

    @Operation(
            summary = "Save a movie or show to the database by TMDB ID",
            description = "Fetches full details from TMDB and saves to DB. If already saved, returns the existing record. Get the tmdbId from search results (numeric part only — e.g. 27205, NOT 'movie-27205').",
            parameters = {
                    @Parameter(name = "tmdbId", description = "TMDB numeric ID", required = true, example = "27205"),
                    @Parameter(name = "type",   description = "movie or tv", required = true, example = "movie")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Saved (or already existing) movie/show",
                            content = @Content(schema = @Schema(implementation = Movie.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid type (must be movie or tv)", content = @Content),
                    @ApiResponse(responseCode = "500", description = "TMDB API error", content = @Content)
            }
    )
    @PostMapping("/movies/save")
    public ResponseEntity<?> saveMovie(
            @RequestParam String tmdbId,
            @RequestParam String type) {

        if (!type.equals("movie") && !type.equals("tv"))
            return ResponseEntity.badRequest().body("type must be 'movie' or 'tv'");

        String compositeId = type + "-" + tmdbId;

        try {
            Movie existing = movieRepository.findByTmdbId(compositeId).orElse(null);
            if (existing != null) return ResponseEntity.ok(existing);

            Movie fetched = type.equals("movie")
                    ? tmdbService.getMovieById(tmdbId)
                    : tmdbService.getShowById(tmdbId);
            movieRepository.save(fetched);
            return ResponseEntity.ok(fetched);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("TMDB fetch failed: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- GET

    @Operation(
            summary = "Get all saved movies and shows",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Full list",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Movie.class))))
            }
    )
    @GetMapping("/movies")
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    @Operation(
            summary = "Get only saved movies (mediaType = MOVIE)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of movies",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Movie.class))))
            }
    )
    @GetMapping("/movies/type/movie")
    public List<Movie> getMoviesOnly() {
        return movieRepository.findByMediaTypeIgnoreCase("MOVIE");
    }

    @Operation(
            summary = "Get only saved TV shows (mediaType = SHOW)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of shows",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Movie.class))))
            }
    )
    @GetMapping("/movies/type/show")
    public List<Movie> getShowsOnly() {
        return movieRepository.findByMediaTypeIgnoreCase("SHOW");
    }

    @Operation(
            summary = "Get a saved movie or show by DB ID",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = @Content(schema = @Schema(implementation = Movie.class))),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping("/movies/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return movieRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(
            summary = "Get saved movies/shows by genre",
            parameters = {
                    @Parameter(name = "genre", description = "Genre name (case-insensitive)", required = true, example = "Drama")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Filtered list",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Movie.class))))
            }
    )
    @GetMapping("/movies/genre/{genre}")
    public List<Movie> getByGenre(@PathVariable String genre) {
        return movieRepository.findByGenreIgnoreCase(genre);
    }

    @Operation(
            summary = "Get all reviews for a movie or show",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of reviews"),
                    @ApiResponse(responseCode = "404", description = "Movie/show not found", content = @Content)
            }
    )
    @GetMapping("/movies/{id}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long id) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        return ResponseEntity.ok(movie.getReviews());
    }

    // UPDATE / DELETE

    @Operation(
            summary = "Recalculate and update average rating",
            description = "Averages all linked review ratings and saves the result to the rating field.",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rating updated"),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/movies/{id}/update-rating")
    public ResponseEntity<?> updateRating(@PathVariable Long id) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");

        List<Review> reviews = movie.getReviews();
        if (reviews.isEmpty()) return ResponseEntity.ok("No reviews yet — rating unchanged");

        int avg = reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
        movie.setRating(avg);
        movieRepository.save(movie);
        return ResponseEntity.ok("Rating updated to " + avg);
    }

    @Operation(
            summary = "Update movie/show details manually",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated",
                            content = @Content(schema = @Schema(implementation = Movie.class))),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping("/movies/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody Movie updated) {
        Movie existing = movieRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");

        existing.setTitle(updated.getTitle());
        existing.setGenre(updated.getGenre());
        existing.setReleaseDate(updated.getReleaseDate());
        existing.setDirector(updated.getDirector());
        existing.setOverview(updated.getOverview());
        existing.setPosterUrl(updated.getPosterUrl());
        existing.setTmdbScore(updated.getTmdbScore());

        movieRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @Operation(
            summary = "Delete a movie or show",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @Transactional
    @DeleteMapping("/movies/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        if (!movieRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        movieRepository.deleteById(id);
        return ResponseEntity.ok("Deleted id=" + id);
    }
}
