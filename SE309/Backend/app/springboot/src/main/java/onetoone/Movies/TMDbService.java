package onetoone.Movies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Talks to The Movie Database (TMDB) API.
 *
 * Docs: https://developer.themoviedb.org/docs
 *
 * This service handles both Movies (type="movie") and TV Shows (type="tv").
 */
@Service
public class TMDbService {

    private static final String BASE_URL  = "https://api.themoviedb.org/3";
    private static final String IMG_BASE  = "https://image.tmdb.org/t/p/w500";

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // ---------------------------------------------------------------- SEARCH

    /**
     * Search for movies.
     * Maps to: GET /search/movie?query=...
     */
    public List<Movie> searchMovies(String query, int maxResults) {
        return search(query, "movie", maxResults);
    }

    /**
     * Search for TV shows.
     * Maps to: GET /search/tv?query=...
     */
    public List<Movie> searchShows(String query, int maxResults) {
        return search(query, "tv", maxResults);
    }

    private List<Movie> search(String query, String type, int maxResults) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(BASE_URL + "/search/" + type)
                    .queryParam("api_key", apiKey)
                    .queryParam("query", query)
                    .queryParam("page", 1)
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);
            JsonNode results = root.get("results");

            List<Movie> movies = new ArrayList<>();
            if (results == null || !results.isArray()) return movies;

            int limit = Math.min(maxResults, results.size());
            for (int i = 0; i < limit; i++) {
                Movie m = parseSearchResult(results.get(i), type);
                if (m != null) movies.add(m);
            }
            return movies;
        } catch (Exception e) {
            throw new RuntimeException("TMDB search failed (" + type + "): " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- DETAIL

    /**
     * Fetch full movie details by TMDB numeric ID.
     * Maps to: GET /movie/{id}?append_to_response=credits
     */
    public Movie getMovieById(String tmdbNumericId) {
        return getDetail(tmdbNumericId, "movie");
    }

    /**
     * Fetch full TV show details by TMDB numeric ID.
     * Maps to: GET /tv/{id}?append_to_response=credits
     */
    public Movie getShowById(String tmdbNumericId) {
        return getDetail(tmdbNumericId, "tv");
    }

    private Movie getDetail(String numericId, String type) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(BASE_URL + "/" + type + "/" + numericId)
                    .queryParam("api_key", apiKey)
                    .queryParam("append_to_response", "credits")
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);

            Movie m = new Movie();
            m.setTmdbId(type + "-" + numericId);
            m.setMediaType(type.equals("movie") ? "MOVIE" : "SHOW");

            // Title: movies use "title", shows use "name"
            m.setTitle(type.equals("movie")
                    ? textOrNull(root.get("title"))
                    : textOrNull(root.get("name")));

            // Release date: movies use "release_date", shows use "first_air_date"
            m.setReleaseDate(type.equals("movie")
                    ? textOrNull(root.get("release_date"))
                    : textOrNull(root.get("first_air_date")));

            // Overview
            String overview = textOrNull(root.get("overview"));
            if (overview != null && overview.length() > 2000) overview = overview.substring(0, 1997) + "...";
            m.setOverview(overview);

            // Poster
            String posterPath = textOrNull(root.get("poster_path"));
            if (posterPath != null) m.setPosterUrl(IMG_BASE + posterPath);

            // TMDB score
            JsonNode vote = root.get("vote_average");
            if (vote != null && !vote.isNull()) m.setTmdbScore(vote.asDouble());

            // Genre — first in the genres array
            m.setGenre(firstNameFromArray(root.get("genres")));

            // Director (movies) or creator (shows) from credits
            m.setDirector(extractDirectorOrCreator(root, type));

            return m;
        } catch (Exception e) {
            throw new RuntimeException("TMDB detail fetch failed (type=" + type + ", id=" + numericId + "): " + e.getMessage(), e);
        }
    }

    // HELPERS

    private Movie parseSearchResult(JsonNode node, String type) {
        if (node == null) return null;

        String numericId = textOrNull(node.get("id"));
        String title = type.equals("movie")
                ? textOrNull(node.get("title"))
                : textOrNull(node.get("name"));

        if (numericId == null || title == null) return null;

        Movie m = new Movie();
        m.setTmdbId(type + "-" + numericId);
        m.setMediaType(type.equals("movie") ? "MOVIE" : "SHOW");
        m.setTitle(title);

        m.setReleaseDate(type.equals("movie")
                ? textOrNull(node.get("release_date"))
                : textOrNull(node.get("first_air_date")));

        String posterPath = textOrNull(node.get("poster_path"));
        if (posterPath != null) m.setPosterUrl(IMG_BASE + posterPath);

        JsonNode vote = node.get("vote_average");
        if (vote != null && !vote.isNull()) m.setTmdbScore(vote.asDouble());

        // genre_ids are just numbers in search; genre name requires a separate lookup — skip for search
        return m;
    }

    private String extractDirectorOrCreator(JsonNode root, String type) {
        if (type.equals("movie")) {
            // credits.crew[] where job == "Director"
            JsonNode crew = root.path("credits").path("crew");
            if (crew.isArray()) {
                for (JsonNode member : crew) {
                    if ("Director".equals(textOrNull(member.get("job")))) {
                        return textOrNull(member.get("name"));
                    }
                }
            }
        } else {
            // created_by[0].name
            JsonNode creators = root.get("created_by");
            if (creators != null && creators.isArray() && creators.size() > 0) {
                return textOrNull(creators.get(0).get("name"));
            }
        }
        return null;
    }

    private String firstNameFromArray(JsonNode array) {
        if (array == null || !array.isArray() || array.size() == 0) return null;
        JsonNode first = array.get(0);
        return first != null ? textOrNull(first.get("name")) : null;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String s = node.asText();
        return (s == null || s.isBlank()) ? null : s;
    }
}
