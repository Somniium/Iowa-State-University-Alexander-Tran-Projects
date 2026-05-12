package onetoone;

import onetoone.Books.Book;
import onetoone.Books.HardcoverService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HardcoverServiceTest {

    @Test
    void searchBooksUsesHardcoverSearchFieldInsteadOfDisallowedIlike() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HardcoverService service = new HardcoverService();
        ReflectionTestUtils.setField(service, "apiUrl", "https://api.hardcover.app/v1/graphql");
        ReflectionTestUtils.setField(service, "apiToken", "Bearer test-token");
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        server.expect(once(), requestTo("https://api.hardcover.app/v1/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(content().string(allOf(
                        containsString("search("),
                        not(containsString("_ilike"))
                )))
                .andRespond(withSuccess("""
                        {
                          "data": {
                            "search": {
                              "results": {
                                "found": 1,
                                "hits": [
                                  {
                                    "document": {
                                      "id": 82563,
                                      "title": "Dune",
                                      "author_names": ["Frank Herbert"],
                                      "release_year": 1965,
                                      "description": "A desert planet.",
                                      "image": { "url": "https://example.test/dune.jpg" }
                                    }
                                  }
                                ]
                              }
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<Book> results = service.searchBooks("dune", 5);

        assertEquals(1, results.size());
        assertEquals("82563", results.get(0).getVolumeId());
        assertEquals("Dune", results.get(0).getTitle());
        assertEquals("Frank Herbert", results.get(0).getAuthors());
        assertEquals("1965", results.get(0).getPublishedDate());
        assertEquals("https://example.test/dune.jpg", results.get(0).getThumbnailUrl());
        server.verify();
    }
}
