package onetoone.Games;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the RAWG Video Games Database API.
 *
 * Docs: https://api.rawg.io/docs/
 */
@Service
public class RawgService {

    private static final String BASE_URL = "https://api.rawg.io/api";

    @Value("${rawg.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Search for games by name.
     * Maps to: GET /games?search={query}&page_size={max}&key={key}
     */
    public List<Game> searchGames(String query, int maxResults) {
        try {
            int size = Math.min(Math.max(maxResults, 1), 40);

            String url = UriComponentsBuilder
                    .fromHttpUrl(BASE_URL + "/games")
                    .queryParam("key", apiKey)
                    .queryParam("search", query)
                    .queryParam("ordering", "-relevance")
                    .queryParam("page_size", size)
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);
            JsonNode results = root.get("results");

            List<Game> games = new ArrayList<>();
            if (results == null || !results.isArray()) return games;

            for (JsonNode node : results) {
                Game g = parseGameNode(node);
                if (g != null) games.add(g);
            }

            // Sort: exact title match first, then starts-with, then contains, then RAWG order
            String q = query.toLowerCase().trim();
            games.sort((a, b) -> Integer.compare(titleScore(b.getTitle(), q), titleScore(a.getTitle(), q)));

            return games;
        } catch (Exception e) {
            throw new RuntimeException("RAWG search failed: " + e.getMessage(), e);
        }
    }
    /**
     * Title Score helper method
     *
     */
    private int titleScore(String title, String query) {
        if (title == null) return 0;
        String t = title.toLowerCase().trim();
        if (t.equals(query))     return 3; // exact match
        if (t.startsWith(query)) return 2; // e.g. "red dead redemption 2" for query "red dead redemption"
        if (t.contains(query))   return 1; // contains
        return 0;
    }

    /**
     * Fetch full details for a game by its RAWG ID.
     * Maps to: GET /games/{id}?key={key}
     */
    public Game getGameByRawgId(String rawgId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(BASE_URL + "/games/" + rawgId)
                    .queryParam("key", apiKey)
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);

            Game g = new Game();
            g.setRawgId(textOrNull(root.get("id")));
            g.setTitle(textOrNull(root.get("name")));
            g.setReleaseDate(textOrNull(root.get("released")));
            g.setCoverUrl(textOrNull(root.get("background_image")));

            // Metacritic score
            JsonNode mc = root.get("metacritic");
            if (mc != null && !mc.isNull()) g.setMetacriticScore(mc.asInt());

            // Description (detail endpoint has description_raw)
            String desc = textOrNull(root.get("description_raw"));
            if (desc != null && desc.length() > 2000) desc = desc.substring(0, 1997) + "...";
            g.setDescription(desc);

            // Genres: array of { id, name }
            g.setGenre(firstNameFromArray(root.get("genres")));

            // Developers: array of { id, name }
            g.setDeveloper(firstNameFromArray(root.get("developers")));

            return g;
        } catch (Exception e) {
            throw new RuntimeException("RAWG fetch failed for id=" + rawgId + ": " + e.getMessage(), e);
        }
    }

    // ---- helpers ----

    private Game parseGameNode(JsonNode node) {
        if (node == null) return null;

        String rawgId = textOrNull(node.get("id"));
        String title  = textOrNull(node.get("name"));
        if (rawgId == null || title == null) return null;

        Game g = new Game();
        g.setRawgId(rawgId);
        g.setTitle(title);
        g.setReleaseDate(textOrNull(node.get("released")));
        g.setCoverUrl(textOrNull(node.get("background_image")));

        JsonNode mc = node.get("metacritic");
        if (mc != null && !mc.isNull()) g.setMetacriticScore(mc.asInt());

        g.setGenre(firstNameFromArray(node.get("genres")));

        // Search results don't include developer — will be null until saved via detail endpoint
        return g;
    }

    /** Returns the name of the first element in a RAWG array like [{ "id": 1, "name": "Action" }] */
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
