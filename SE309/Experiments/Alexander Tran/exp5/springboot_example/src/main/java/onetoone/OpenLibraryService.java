package onetoone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenLibraryService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Search Open Library.
     * Example upstream:
     *   https://openlibrary.org/search.json?q=harry+potter&limit=5
     *
     * This returns good author + ISBN coverage compared to Google.
     * Description is usually NOT included in search results; fetch via getBookByVolumeId() for that.
     */
    public List<Book> searchBooks(String query, int maxResults) {
        try {
            int limit = Math.min(Math.max(maxResults, 1), 100);

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://openlibrary.org/search.json")
                    .queryParam("q", query)
                    .queryParam("limit", limit)
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);

            List<Book> results = new ArrayList<>();
            JsonNode docs = root.get("docs");
            if (docs == null || !docs.isArray()) return results;

            for (JsonNode doc : docs) {
                Book b = parseSearchDoc(doc);
                if (b != null) results.add(b);
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Open Library search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch details for a work (recommended) or edition key.
     * Work key looks like: /works/OL82563W
     * Edition key looks like: /books/OLxxxxM
     *
     * NOTE: Work endpoints often contain description.
     */
    public Book getBookByVolumeId(String openLibraryKey) {
        try {
            if (openLibraryKey == null || openLibraryKey.isBlank()) {
                throw new IllegalArgumentException("openLibraryKey is required");
            }

            String key = openLibraryKey.trim();
            String url = "https://openlibrary.org" + key + ".json";

            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(json);

            // Build a Book using what we can from the work/edition endpoint
            Book b = new Book();
            b.setVolumeId(key);
            b.setTitle(textOrNull(root.get("title")));

            // description can be string OR { "type": "...", "value": "..." }
            b.setDescription(extractDescription(root.get("description")));

            // Published date varies a lot on OL; for works it may be missing
            b.setPublishedDate(textOrNull(root.get("first_publish_date")));

            // Covers: "covers": [12345, ...]
            JsonNode covers = root.get("covers");
            if (covers != null && covers.isArray() && covers.size() > 0) {
                int coverId = covers.get(0).asInt();
                b.setThumbnailUrl(coverUrlByCoverId(coverId));
            }

            // Authors on a WORK endpoint is an array of objects with {author: {key: ...}}
            // To keep this simple, we won't do extra network calls here.
            // Authors will be populated reliably based on search results.

            return b;
        } catch (Exception e) {
            throw new RuntimeException("Open Library fetch failed: " + e.getMessage(), e);
        }
    }

    // ---------------- helpers ----------------

    private Book parseSearchDoc(JsonNode doc) {
        if (doc == null) return null;

        String key = textOrNull(doc.get("key")); // usually "/works/OL..."
        String title = textOrNull(doc.get("title"));

        if (key == null || title == null) return null;

        Book b = new Book();
        b.setVolumeId(key);
        b.setTitle(title);

        // author_name: ["J. K. Rowling", ...]
        JsonNode authorNames = doc.get("author_name");
        if (authorNames != null && authorNames.isArray()) {
            List<String> names = new ArrayList<>();
            for (JsonNode a : authorNames) names.add(a.asText());
            if (!names.isEmpty()) b.setAuthors(String.join(", ", names));
        }

        // first_publish_year: 1997
        JsonNode year = doc.get("first_publish_year");
        if (year != null && year.isNumber()) {
            b.setPublishedDate(String.valueOf(year.asInt()));
        }

        // isbn: ["978...", "0439...", ...] - pick a 13-digit if possible, else first
        JsonNode isbns = doc.get("isbn");
        if (isbns != null && isbns.isArray()) {
            String best = pickBestIsbn(isbns);
            b.setIsbn13(best);
        }

        // cover_i: 12345 (cover id)
        JsonNode coverI = doc.get("cover_i");
        if (coverI != null && coverI.isNumber()) {
            b.setThumbnailUrl(coverUrlByCoverId(coverI.asInt()));
        }

        // description is not typically in search docs -> keep null
        return b;
    }

    private String pickBestIsbn(JsonNode isbns) {
        String fallback = null;
        for (JsonNode n : isbns) {
            String s = (n == null || n.isNull()) ? null : n.asText();
            if (s == null || s.isBlank()) continue;
            if (fallback == null) fallback = s;

            // prefer 13-digit numeric ISBNs
            if (s.length() == 13) return s;
        }
        return fallback;
    }

    private String coverUrlByCoverId(int coverId) {
        // You can switch -M to -L for large covers
        return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
    }

    private String extractDescription(JsonNode descriptionNode) {
        if (descriptionNode == null || descriptionNode.isNull()) return null;

        // Sometimes it's directly a string
        if (descriptionNode.isTextual()) {
            String s = descriptionNode.asText();
            return (s == null || s.isBlank()) ? null : s;
        }

        // Sometimes it's an object: { "type": "...", "value": "..." }
        JsonNode value = descriptionNode.get("value");
        if (value != null && value.isTextual()) {
            String s = value.asText();
            return (s == null || s.isBlank()) ? null : s;
        }

        return null;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String s = node.asText();
        return (s == null || s.isBlank()) ? null : s;
    }
}