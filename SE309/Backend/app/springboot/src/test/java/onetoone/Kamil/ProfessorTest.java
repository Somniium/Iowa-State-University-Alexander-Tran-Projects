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
public class ProfessorTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllProfessorsTest() {
        given()
                .when()
                .get("/professors")
                .then()
                .statusCode(200);
    }

    @Test
    public void createProfessorAndDeleteTest() {
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

        // Create professor
        Long profId = ((Number) given()
                .contentType(ContentType.JSON)
                .pathParam("id", userId)
                .when()
                .post("/professor/{id}").then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        // Add class to professor
        String className = "COMS309";

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .pathParam("className", className)
                .when()
                .put("/professor/add-class/{id}/{className}").then()
                .statusCode(200);

        // Remove class from professor
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .pathParam("className", className)
                .when()
                .put("/professor/remove-class/{id}/{className}").then()
                .statusCode(200);

        // Delete professor
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .when()
                .delete("/professor/remove-professor/id/{id}").then()
                .statusCode(200);
    }

    @Test
    public void createProfessorGetByUserAndIDAndRemoveTest() {
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

        // Create professor
        Long profId = ((Number) given()
                .contentType(ContentType.JSON)
                .pathParam("id", userId)
                .when()
                .post("/professor/{id}").then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        // GET professor by ID
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .when()
                .get("/professor/id/{id}").then()
                .statusCode(200);

        // GET professor by user
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
                .when()
                .get("/professor/user/{userId}").then()
                .statusCode(200);

        // GET professor groups
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", profId)
                .when()
                .get("/get-professor-groups/{id}").then()
                .statusCode(200);

        // DELETE using user id
        // GET professor by user
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
                .when()
                .delete("/professor/remove-professor/user/{userId}").then()
                .statusCode(200);
    }
}
