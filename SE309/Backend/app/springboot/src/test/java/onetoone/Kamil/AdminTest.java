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
public class AdminTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllAdminsTest() {
        given()
                .when()
                .get("/admins")
                .then()
                .statusCode(200);
    }

    @Test
    public void RegisterUserAndMakeAdminTest() {
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

        // Make the user an Admin
        long adminId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(userJSON)
                .pathParam("id", userId)
                .when()
                .post("/admin/{id}").then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        // Make the admin a master
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", adminId)
                .when()
                .put("/admin-make-master/{id}").then()
                .statusCode(200)
                .body("master", equalTo(true));

        // Remove master user
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", adminId)
                .when()
                .put("/admin-remove-master/{id}").then()
                .statusCode(200)
                .body("master", equalTo(false));
    }

    @Test
    public void createAdminReturnUserThenDeleteTest() {
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

        // Make the user an Admin
        long adminId = ((Number) given()
                .contentType(ContentType.JSON)
                .body(userJSON)
                .pathParam("id", userId)
                .when()
                .post("/admin/{id}").then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        // Gets the admin using the user attached to it.
        given()
                .pathParam("userId", userId)
                .when()
                .get("/admin/user/{userId}")
                .then()
                .statusCode(200)
                .body("id", equalTo((int)adminId));

        // Gets the user of the admin
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", adminId)
                .when()
                .get("/admin/get-user/{id}").then()
                .statusCode(200);

        // Remove admin
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", adminId)
                .when()
                .delete("/remove-admin/id/{id}").then()
                .statusCode(200);
    }
}
