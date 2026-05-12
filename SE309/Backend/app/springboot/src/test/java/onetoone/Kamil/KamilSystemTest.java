package onetoone.Kamil;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KamilSystemTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void registerAndCheckUserExistsTest() {
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


        // GET the test user to see if it was saved
        given()
                .pathParam("name", name)
                .when()
                .get("/users/name/{name}")
                .then()
                .statusCode(200)
                .body("emailId", equalTo(email));
    }

    @Test
    public void registerAndDeleteUserFromDBTest() {
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

        //DEL the test user from the DB.

        given()
                .pathParam("emailId", email)
                .when()
                .delete("/users/email/{emailId}")
                .then()
                .statusCode(200)
                .body("message", equalTo("success"));
    }

    @Test
    public void CreateUsersAndFollowRelationshipTest() {
        Random rand = new Random();
        int randId1 = rand.nextInt(1000000);
        int randId2 = rand.nextInt(1000000);

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

        // Test to see if user1 following user2 works
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId1)
                .pathParam("followId", userId2)
                .when()
                .post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then()
                .statusCode(201);
    }

    @Test
    public void CreateUsersAndFollowersAndTestBlockUser() {
        Random rand = new Random();
        int randId1 = rand.nextInt(1000000);
        int randId2 = rand.nextInt(1000000);

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

        long followerId =
                ((Number) given()
                        .contentType(ContentType.JSON)
                        .pathParam("userId", userId1)
                        .pathParam("followId", userId2)
                        .when()
                        .post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("id")).longValue();

        given()
                .contentType(ContentType.JSON)
                .pathParam("followerId", followerId)
                .when()
                .put("/follower/block-follower/{followerId}")
                .then()
                .statusCode(200);
    }
}
