package onetoone.Kamil;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProfileTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllProfilesTest() {
        given()
                .when()
                .get("/profiles")
                .then()
                .statusCode(200);
    }

    @Test
    public void createProfileGetAndDeleteTest() {
        Random rand = new Random();
        int randId = rand.nextInt(1000000);

        String name = "KamilTestUser" + randId;
        String email = "kamiltest" + randId + "@iastate.edu";

        String userJSON =
                "{" +
                        "\"name\": \"" + name + "\"," +
                        "\"emailId\": \"" + email + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        String profileJSON = "{" +
                "\"bio\": \"Computer Engineering student at ISU\"," +
                "\"hobbies\": \"Music, Gaming\"," +
                "\"gradDate\": \"May 2027\"," +
                "\"linkedInURL\": \"linkedin.com/in/testuser\"" +
                "}";

        // POST the test user to the database
        given()
                .contentType(ContentType.JSON)
                .body(userJSON)
                .when()
                .post("/users").then()
                .statusCode(200)
                .body("message", equalTo("success"));

        // Extract user ID
        int userId = given()
                .pathParam("emailId", email)
                .when()
                .get("/users/email/{emailId}")
                .then()
                .extract()
                .path("id");

        // POST to create profile
        long profId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(profileJSON)
                .pathParam("id", userId)
                .when()
                .post("/profile/userId/{id}").then()
                .statusCode(201)
                .extract()
                .path("profileId")).longValue();

        // GET profile by ID
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .when()
                .get("/profile/profileId/{id}").then()
                .statusCode(200);

        // GET profile by user ID
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", userId)
                .when()
                .get("/profile/userId/{id}").then()
                .statusCode(200);
    }

    @Test
    public void createProfileUpdateThenDeleteTest() {
        Random rand = new Random();
        int randId = rand.nextInt(1000000);

        String name = "KamilTestUser" + randId;
        String email = "kamiltest" + randId + "@iastate.edu";

        String userJSON =
                "{" +
                        "\"name\": \"" + name + "\"," +
                        "\"emailId\": \"" + email + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        String profileJSON = "{" +
                "\"bio\": \"Computer Engineering student at ISU\"," +
                "\"hobbies\": \"Music, Gaming\"," +
                "\"gradDate\": \"May 2027\"," +
                "\"linkedInURL\": \"linkedin.com/in/testuser\"" +
                "}";

        String updatedProfileJSON = "{" +
                "\"bio\": \"Computer Engineering student at ISU\"," +
                "\"hobbies\": \"Music, Gaming\"," +
                "\"gradDate\": \"May 2029\"," +
                "\"linkedInURL\": \"linkedin.com/in/testuser\"" +
                "}";

        // POST the test user to the database
        given()
                .contentType(ContentType.JSON)
                .body(userJSON)
                .when()
                .post("/users").then()
                .statusCode(200)
                .body("message", equalTo("success"));

        // Extract user ID
        int userId = given()
                .pathParam("emailId", email)
                .when()
                .get("/users/email/{emailId}")
                .then()
                .extract()
                .path("id");

        // POST to create profile
        long profId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(profileJSON)
                .pathParam("id", userId)
                .when()
                .post("/profile/userId/{id}").then()
                .statusCode(201)
                .extract()
                .path("profileId")).longValue();

        // PUT to update profile
        given()
                .contentType(ContentType.JSON)
                .body(updatedProfileJSON)
                .pathParam("id", profId)
                .when()
                .put("/update-profile/{id}").then()
                .statusCode(200)
                .body("gradDate", equalTo("May 2029"));

        // DELETE profile
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .when()
                .delete("/delete-profile/{id}").then()
                .statusCode(204);
    }
}
