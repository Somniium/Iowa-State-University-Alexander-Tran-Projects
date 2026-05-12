package onetoone.Chat;

import onetoone.Books.Book;
import onetoone.Books.BookRepository;
import onetoone.Games.Game;
import onetoone.Games.GameRepository;
import onetoone.Movies.Movie;
import onetoone.Movies.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds a CyVal-aware system prompt for the chatbot.
 *
 * Pulls top-rated items from the catalog and stuffs them into the system prompt
 * when the user's intent suggests recommendations are wanted. For everything else,
 * a smaller catalog summary keeps token usage reasonable.
 */
@Component
public class CyvalContextBuilder {

    @Autowired private MovieRepository movieRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private BookRepository bookRepository;

    private static final int TOP_N = 5;

    /** Reused PageRequest for "top 5 by rating descending" — sort + LIMIT pushed to SQL. */
    private static final Pageable TOP_BY_RATING =
            PageRequest.of(0, TOP_N, Sort.by(Sort.Direction.DESC, "rating"));

    public String buildSystemPrompt(Long userId, String userText) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are CyVal's assistant, helping Iowa State students discover and review games, movies, TV shows, and books. ");
        sb.append("Ratings on CyVal use a 0-100 scale (higher is better). ");
        sb.append("Never use emojis in your responses, just text. ");
        sb.append("You can: (1) recommend media from CyVal's catalog, (2) chat generally about media, ");
        sb.append("(3) help users write and improve their reviews. ");
        sb.append("Keep responses concise and friendly. When you reference a title, prefer items from the CyVal catalog below.\n\n");

        boolean wantsRecs = looksLikeRecommendationRequest(userText);
        boolean wantsReviewHelp = looksLikeReviewHelp(userText);

        if (wantsRecs) {
            appendTopMovies(sb);
            appendTopShows(sb);
            appendTopGames(sb);
            appendTopBooks(sb);
        }

        if (wantsReviewHelp) {
            sb.append("\nWhen helping with reviews: keep tone constructive, suggest concrete improvements ");
            sb.append("(specific examples, balanced pros/cons, avoid spoilers without warning), and never write the review for them — guide them.\n");
        }

        return sb.toString();
    }

    // INTENT DETECTION

    private boolean looksLikeRecommendationRequest(String text) {
        if (text == null) return false;
        String t = text.toLowerCase();
        return t.contains("recommend") || t.contains("suggest") || t.contains("what should i")
                || t.contains("any good") || t.contains("similar to") || t.contains("like ");
    }

    private boolean looksLikeReviewHelp(String text) {
        if (text == null) return false;
        String t = text.toLowerCase();
        return t.contains("my review") || t.contains("rewrite") || t.contains("improve")
                || t.contains("edit") || t.contains("polish");
    }

    // CATALOG INJECTION

    private void appendTopMovies(StringBuilder sb) {
        // SQL does the ORDER BY + LIMIT — we never load the full movie catalog into memory.
        List<Movie> movies = movieRepository.findByMediaTypeIgnoreCaseOrderByRatingDesc("MOVIE", TOP_BY_RATING);
        if (movies.isEmpty()) return;
        sb.append("Top-rated MOVIES on CyVal:\n");
        for (Movie m : movies) {
            sb.append("  - ").append(m.getTitle())
                    .append(" (").append(safe(m.getGenre())).append(", ").append(m.getRating()).append("/100)\n");
        }
        sb.append("\n");
    }

    private void appendTopShows(StringBuilder sb) {
        List<Movie> shows = movieRepository.findByMediaTypeIgnoreCaseOrderByRatingDesc("SHOW", TOP_BY_RATING);
        if (shows.isEmpty()) return;
        sb.append("Top-rated TV SHOWS on CyVal:\n");
        for (Movie s : shows) {
            sb.append("  - ").append(s.getTitle())
                    .append(" (").append(safe(s.getGenre())).append(", ").append(s.getRating()).append("/100)\n");
        }
        sb.append("\n");
    }

    private void appendTopGames(StringBuilder sb) {
        // findAll(Pageable) issues a single ORDER BY rating DESC LIMIT 5 query.
        List<Game> games = gameRepository.findAll(TOP_BY_RATING).getContent();
        if (games.isEmpty()) return;
        sb.append("Top-rated GAMES on CyVal:\n");
        for (Game g : games) {
            sb.append("  - ").append(g.getTitle())
                    .append(" (").append(safe(g.getGenre())).append(", ").append(g.getRating()).append("/100)\n");
        }
        sb.append("\n");
    }

    private void appendTopBooks(StringBuilder sb) {
        List<Book> books = bookRepository.findAll(TOP_BY_RATING).getContent();
        if (books.isEmpty()) return;
        sb.append("Top-rated BOOKS on CyVal:\n");
        for (Book b : books) {
            sb.append("  - ").append(b.getTitle())
                    .append(" by ").append(safe(b.getAuthors()))
                    .append(" (").append(b.getRating()).append("/100)\n");
        }
        sb.append("\n");
    }

    private String safe(String s) {
        return s == null ? "Unknown" : s;
    }
}