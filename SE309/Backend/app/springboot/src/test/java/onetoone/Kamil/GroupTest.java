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
public class GroupTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllGroupsTest() {
        given()
                .when()
                .get("/groups")
                .then()
                .statusCode(200);
    }

    @Test
    public void createProfessorAndGroupAndPerformOperationsTest() {
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

        // POST a group
        String groupName = className + " " + randId;
        Long groupId = ((Number) given()
                .contentType(ContentType.JSON)
                .pathParam("groupName", groupName)
                .pathParam("profId", profId)
                .pathParam("className", className)
                .when()
                .post("/group/name/{groupName}/professor/{profId}/class/{className}").then()
                .statusCode(201)
                .extract()
                .path("groupId")).longValue();

        // GET by id
        given()
                .pathParam("id", groupId)
                .when()
                .get("/group/id/{id}")
                .then()
                .statusCode(200);

        // GET by name
        given()
                .pathParam("name", groupName)
                .when()
                .get("/group/name/{name}")
                .then()
                .statusCode(200)
                .body("name", equalTo(groupName));
    }

    @Test
    public void createGroupThenAddAndRemoveUserAndDelete() {
        Random rand = new Random();
        int randId1 = rand.nextInt(100000000);
        int randId2 = rand.nextInt(100000000);

        String name1 = "KamilTestUser" + randId1;
        String email1 = "kamiltest" + randId1 + "@iastate.edu";

        String name2 = "AlexTestUser" + randId2;
        String email2 = "alextest" + randId2 + "@iastate.edu";

        String userJSON1 =
                "{" +
                        "\"name\": \"" + name1 + "\"," +
                        "\"emailId\": \"" + email1 + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        String userJSON2 =
                "{" +
                        "\"name\": \"" + name2 + "\"," +
                        "\"emailId\": \"" + email2 + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        // POST the test users to the database

        // User 1
        given()
                .contentType(ContentType.JSON)
                .body(userJSON1)
                .when()
                .post("/users").then()
                .statusCode(200)
                .body("message", equalTo("success"));

        // User 2
        given()
                .contentType(ContentType.JSON)
                .body(userJSON2)
                .when()
                .post("/users").then()
                .statusCode(200)
                .body("message", equalTo("success"));

        int userId1 =
                given()
                        .pathParam("emailId", email1)
                        .when()
                        .get("/users/email/{emailId}")
                        .then()
                        .extract()
                        .path("id");

        int userId2 =
                given()
                        .pathParam("emailId", email2)
                        .when()
                        .get("/users/email/{emailId}")
                        .then()
                        .extract()
                        .path("id");

        // Create professor
        Long profId = ((Number) given()
                .contentType(ContentType.JSON)
                .pathParam("id", userId1)
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

        // POST a group
        String groupName = className + " " + randId1;
        Long groupId = ((Number) given()
                .contentType(ContentType.JSON)
                .pathParam("groupName", groupName)
                .pathParam("profId", profId)
                .pathParam("className", className)
                .when()
                .post("/group/name/{groupName}/professor/{profId}/class/{className}").then()
                .statusCode(201)
                .extract()
                .path("groupId")).longValue();

        // ADD a user to the group
        given()
                .contentType(ContentType.JSON)
                .pathParam("groupId", groupId)
                .pathParam("userId", userId2)
                .when()
                .post("/add-user-to-group/group/{groupId}/user/{userId}").then()
                .statusCode(201);

        // REMOVE user from the group
        given()
                .contentType(ContentType.JSON)
                .pathParam("groupId", groupId)
                .pathParam("userId", userId2)
                .when()
                .delete("/remove-user-from-group/group/{groupId}/user/{userId}").then()
                .statusCode(200);

        // DELETE group
        given()
                .contentType(ContentType.JSON)
                .pathParam("id", groupId)
                .when()
                .delete("/delete-group/{id}").then()
                .statusCode(200);
    }
}
