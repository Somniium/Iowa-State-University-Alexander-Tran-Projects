package onetoone.Books;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the Hardcover GraphQL API for book data.
 *
 * Credentials are in application.properties:
 *
 * Hardcover docs: https://hardcover.app/account/api
 *
 * Portion of code was provided by Hardcover.
 */
@Service
public class HardcoverService {

    @Value("${hardcover.api.url}")
    private String apiUrl;

    @Value("${hardcover.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Search Hardcover for books by title.
     * Results are NOT saved to DB — used for browsing before a user picks one.
     */
    public List<Book> searchBooks(String query, int maxResults) {
        int limit = Math.min(Math.max(maxResults, 1), 40);

        ObjectNode variables = mapper.createObjectNode();
        variables.put("q", query.trim());
        variables.put("limit", limit);

        ObjectNode request = mapper.createObjectNode();
        request.put("query", "query SearchBooks($q: String!, $limit: Int!) { search(query: $q, query_type: \"Book\", per_page: $limit, page: 1) { results } }");
        request.set("variables", variables);

        try {
            JsonNode root = post(request.toString());
            JsonNode searchResults = root.path("data").path("search").path("results");
            JsonNode books = searchResults.path("hits");
            if (!books.isArray()) {
                books = searchResults;
            }
            List<Book> results = new ArrayList<>();
            if (books.isArray()) {
                for (JsonNode node : books) {
                    JsonNode bookNode = node.has("document") ? node.path("document") : node;
                    Book b = parseBookNode(bookNode);
                    if (b != null) results.add(b);
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Hardcover search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch full details for a single book by its Hardcover numeric ID.
     */
    public Book getBookById(String hardcoverId) {
        String graphql = """
            {
              "query": "query GetBook($id: Int!) { books_by_pk(id: $id) { id title contributions { author { name } } release_year description image { url } book_series { series { name } } } }",
              "variables": { "id": %s }
            }
            """.formatted(hardcoverId);

        try {
            JsonNode root = post(graphql);
            JsonNode node = root.path("data").path("books_by_pk");
            if (node.isMissingNode() || node.isNull())
                throw new RuntimeException("Book not found for id=" + hardcoverId);
            return parseBookNode(node);
        } catch (Exception e) {
            throw new RuntimeException("Hardcover fetch failed for id=" + hardcoverId + ": " + e.getMessage(), e);
        }
    }

    // ---- helpers ----

    private JsonNode post(String graphqlJson) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiToken);

        HttpEntity<String> entity = new HttpEntity<>(graphqlJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode errors = root.get("errors");
        if (errors != null && errors.isArray() && errors.size() > 0) {
            throw new RuntimeException(errors.get(0).path("message").asText("GraphQL error"));
        }
        return root;
    }

    private Book parseBookNode(JsonNode node) {
        if (node == null || node.isNull()) return null;

        Book b = new Book();

        // Hardcover uses numeric IDs — store as string in volumeId field for consistency
        JsonNode idNode = node.get("id");
        if (idNode != null && !idNode.isNull()) b.setVolumeId(idNode.asText());

        b.setTitle(textOrNull(node.get("title")));

        // release_year is an int
        JsonNode year = node.get("release_year");
        if (year != null && !year.isNull()) b.setPublishedDate(year.asText());

        // description
        String desc = textOrNull(node.get("description"));
        if (desc != null && desc.length() > 2000) desc = desc.substring(0, 1997) + "...";
        b.setDescription(desc);

        // image.url
        JsonNode image = node.get("image");
        if (image != null && !image.isNull()) b.setThumbnailUrl(textOrNull(image.get("url")));

        // contributions[].author.name
        JsonNode contributions = node.get("contributions");
        if (contributions != null && contributions.isArray() && contributions.size() > 0) {
            List<String> authors = new ArrayList<>();
            for (JsonNode c : contributions) {
                JsonNode author = c.get("author");
                if (author != null) {
                    String name = textOrNull(author.get("name"));
                    if (name != null) authors.add(name);
                }
            }
            if (!authors.isEmpty()) b.setAuthors(String.join(", ", authors));
        }
        if (b.getAuthors() == null) {
            JsonNode authorNames = node.get("author_names");
            if (authorNames != null && authorNames.isArray()) {
                List<String> authors = new ArrayList<>();
                for (JsonNode authorName : authorNames) {
                    String name = textOrNull(authorName);
                    if (name != null) authors.add(name);
                }
                if (!authors.isEmpty()) b.setAuthors(String.join(", ", authors));
            }
        }

        return b;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String s = node.asText();
        return (s == null || s.isBlank()) ? null : s;
    }
}
