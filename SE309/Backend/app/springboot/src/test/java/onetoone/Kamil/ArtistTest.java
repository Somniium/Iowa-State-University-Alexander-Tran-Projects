package onetoone.Kamil;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtistTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllArtistsTest() {
        given()
                .when()
                .get("/artists")
                .then()
                .statusCode(200);
    }

    @Test
    public void searchForArtistSaveToDBAndPerformOperationsTest() {
        // Search for and save artist
        Long artistId = ((Number) given()
                .queryParam("artist_name", "Michael Jackson")
                .when()
                .get("/search-artist-and-save")
                .then()
                .statusCode(200)
                .body("name", notNullValue())
                .extract()
                .path("artistId")).longValue();

        // GET artist from DB
        String spotifyId = given()
                .pathParam("id", artistId)
                .when()
                .get("/artist/get-artist/{id}")
                .then()
                .statusCode(200)
                .body("spotifyId", notNullValue())
                .extract()
                .path("spotifyId");

        // PUT to update artist
        String artistJson = "{" +
                "\"artistId\": " + artistId + "," +
                "\"spotifyId\": \"" + spotifyId + "\"" +
                "\"name\": \"Michael Jackson\"," +
                "\"genre\": \"Unknown\"," +
                "\"photoURL\": \"https://i.scdn.co/image/ab6761610000e5eb997cc9a4aec335d46c9481fd\"," +
                "\"rating\": 100" +
                "}";

        // PUT to update artist rating
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", artistId)
                .when()
                .put("/artist/update-rating/{id}")
                .then()
                .statusCode(200);

        // DELETE artist from the DB
        given()
                .pathParam("id", artistId)
                .when()
                .delete("/artist/delete-artist/id/{id}")
                .then()
                .statusCode(200);
    }
}
