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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AlbumTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllAlbumsTest() {
        given()
                .when()
                .get("/albums")
                .then()
                .statusCode(200);
    }

    @Test
    public void searchForAlbumSaveToDBAndPerformOperationsTest() {
        // Search for and save album
        Long albumId = ((Number) given()
                .queryParam("album_name", "Thriller")
                .when()
                .get("/search-albums-and-save")
                .then()
                .statusCode(200)
                .body("name", notNullValue())
                .extract()
                .path("albumId")).longValue();

        // GET album from DB
        given()
                .pathParam("albumId", albumId)
                .when()
                .get("/album/{albumId}")
                .then()
                .statusCode(200)
                .body("spotifyId", notNullValue());

        // GET album from DB using name
        given()
                .queryParam("name", "Thriller")
                .when()
                .get("/album/name")
                .then()
                .statusCode(200)
                .body("spotifyId", notNullValue());

        // GET album artist from DB
        given()
                .pathParam("albumId", albumId)
                .when()
                .get("/album/get-artist/{albumId}")
                .then()
                .statusCode(200)
                .body("artistId", notNullValue());

        // DELETE album from the DB
        given()
                .pathParam("albumId", albumId)
                .when()
                .delete("/album/{albumId}")
                .then()
                .statusCode(200);
    }

    @Test
    public void addAlbumToDBUpdateThenDeleteTest() {
        // GET album from DB
        Long albumId = ((Number) given()
                .queryParam("album_name", "Thriller")
                .when()
                .get("/search-albums-and-save")
                .then()
                .statusCode(200)
                .body("name", notNullValue())
                .extract()
                .path("albumId")).longValue();

        // GET album from DB
        String spotifyId = given()
                .pathParam("albumId", albumId)
                .when()
                .get("/album/{albumId}")
                .then()
                .statusCode(200)
                .body("spotifyId", notNullValue())
                .extract()
                .path("spotifyId");

        String updatedJSON = "{" +
                "\"albumId\": " + albumId + "," +
                "\"spotifyId\": \"" + spotifyId + "\"," +
                "\"name\": \"Thriller\"," +
                "\"rating\": 100" +
                "}";

        // PUT to update the album
        given()
                .contentType(ContentType.JSON)
                .body(updatedJSON)
                .pathParam("albumId", albumId)
                .when()
                .put("/album/{albumId}")
                .then()
                .statusCode(200)
                .body("rating", equalTo(100));

        // PUT to update rating
        given()
                .contentType(ContentType.JSON)
                .pathParam("albumId", albumId)
                .when()
                .put("/album/update-average-rating/{albumId}")
                .then()
                .statusCode(200);

        // DELETE album from the DB
        given()
                .pathParam("albumId", albumId)
                .when()
                .delete("/album/{albumId}")
                .then()
                .statusCode(200);
    }
}
