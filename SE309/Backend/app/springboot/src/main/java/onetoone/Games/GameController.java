package onetoone.Games;

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
 * Game endpoints — backed by the RAWG API.
 *
 * Typical flow:
 *   1. GET /search-games?q=halo          → browse RAWG results (not saved)
 *   2. POST /games/save?rawgId=3498      → save a game to DB by its RAWG ID
 *   3. PUT /reviews/{id}/game/{gameId}   → link a review to that game (in ReviewController)
 */
@RestController
@Tag(name = "Games", description = "Search the RAWG API and manage saved games. API key required in application.properties (rawg.api.key).")
public class GameController {

    @Autowired private GameRepository gameRepository;
    @Autowired private RawgService rawgService;

    @Operation(
            summary = "Search RAWG for games",
            description = "Queries the RAWG API and returns results WITHOUT saving them to the database. Use this so the user can pick a game before saving.",
            parameters = {
                    @Parameter(name = "q",   description = "Game title to search for", required = true, example = "Red Dead Redemption"),
                    @Parameter(name = "max", description = "Max results to return (1–40, default 5)", example = "5")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of matching games from RAWG",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Game.class))))
            }
    )
    @GetMapping("/search-games")
    public List<Game> searchGames(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "max", defaultValue = "5") int max) {
        return rawgService.searchGames(query, max);
    }

    @Operation(
            summary = "Save a game to the database by RAWG ID",
            description = "Fetches full game details from RAWG by its ID and saves to DB. If already saved, returns the existing record. Get the rawgId from /search-games.",
            parameters = {
                    @Parameter(name = "rawgId", description = "RAWG game ID", required = true, example = "3498")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Saved (or already existing) game",
                            content = @Content(schema = @Schema(implementation = Game.class))),
                    @ApiResponse(responseCode = "500", description = "RAWG API error", content = @Content)
            }
    )
    @PostMapping("/games/save")
    public ResponseEntity<?> saveGame(@RequestParam String rawgId) {
        try {
            Game existing = gameRepository.findByRawgId(rawgId).orElse(null);
            if (existing != null) return ResponseEntity.ok(existing);

            Game fetched = rawgService.getGameByRawgId(rawgId);
            gameRepository.save(fetched);
            return ResponseEntity.ok(fetched);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("RAWG fetch failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get all saved games",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all games in DB",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Game.class))))
            }
    )
    @GetMapping("/games")
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Operation(
            summary = "Get a saved game by DB ID",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID of the game", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Game found",
                            content = @Content(schema = @Schema(implementation = Game.class))),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
            }
    )
    @GetMapping("/games/{id}")
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        return gameRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(
            summary = "Get saved games by genre",
            parameters = {
                    @Parameter(name = "genre", description = "Genre to filter by (case-insensitive)", required = true, example = "Action")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Filtered list of games",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Game.class))))
            }
    )
    @GetMapping("/games/genre/{genre}")
    public List<Game> getGamesByGenre(@PathVariable String genre) {
        return gameRepository.findByGenreIgnoreCase(genre);
    }

    @Operation(
            summary = "Get all reviews for a game",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID of the game", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of reviews for this game"),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
            }
    )
    @GetMapping("/games/{id}/reviews")
    public ResponseEntity<?> getGameReviews(@PathVariable Long id) {
        Game game = gameRepository.findById(id).orElse(null);
        if (game == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        return ResponseEntity.ok(game.getReviews());
    }

    @Operation(
            summary = "Recalculate and update average rating for a game",
            description = "Averages all linked review ratings and saves the result to the game's rating field.",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID of the game", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rating updated",
                            content = @Content(schema = @Schema(example = "Rating updated"))),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
            }
    )
    @PutMapping("/games/{id}/update-rating")
    public ResponseEntity<?> updateRating(@PathVariable Long id) {
        Game game = gameRepository.findById(id).orElse(null);
        if (game == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");

        List<Review> reviews = game.getReviews();
        if (reviews.isEmpty()) return ResponseEntity.ok("No reviews yet — rating unchanged");

        int avg = reviews.stream().mapToInt(Review::getRating).sum() / reviews.size();
        game.setRating(avg);
        gameRepository.save(game);
        return ResponseEntity.ok("Rating updated to " + avg);
    }

    @Operation(
            summary = "Update game details manually",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID of the game", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated game",
                            content = @Content(schema = @Schema(implementation = Game.class))),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
            }
    )
    @PutMapping("/games/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Long id, @RequestBody Game updated) {
        Game existing = gameRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");

        existing.setTitle(updated.getTitle());
        existing.setGenre(updated.getGenre());
        existing.setReleaseDate(updated.getReleaseDate());
        existing.setDeveloper(updated.getDeveloper());
        existing.setDescription(updated.getDescription());
        existing.setCoverUrl(updated.getCoverUrl());
        existing.setMetacriticScore(updated.getMetacriticScore());

        gameRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @Operation(
            summary = "Delete a game",
            parameters = {
                    @Parameter(name = "id", description = "Internal DB ID of the game", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted",
                            content = @Content(schema = @Schema(example = "Deleted game id=1"))),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
            }
    )
    @Transactional
    @DeleteMapping("/games/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        if (!gameRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        gameRepository.deleteById(id);
        return ResponseEntity.ok("Deleted game id=" + id);
    }
}
