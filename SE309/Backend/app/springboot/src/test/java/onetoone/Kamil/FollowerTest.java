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
public class FollowerTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllFollowersTest() {
        given()
                .when()
                .get("/followers")
                .then()
                .statusCode(200);
    }

    @Test
    public void CreateUsersAndFollowRelationshipTest() {
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

        // Test to see if user1 following user2 works
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId1)
                .pathParam("followId", userId2)
                .when()
                .post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then()
                .log().all()
                .statusCode(201);
    }

    @Test
    public void CreateUsersAndFollowersAndTestBlockAndUnblockUser() {
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

        // Checks that user was blocked
        given()
                .contentType(ContentType.JSON)
                .pathParam("followerId", followerId)
                .when()
                .put("/follower/block-follower/{followerId}")
                .then()
                .statusCode(200)
                .body("blocked", equalTo(true));

        // Checks that user was unblocked
        given()
                .contentType(ContentType.JSON)
                .pathParam("followerId", followerId)
                .when()
                .put("/follower/unblock-follower/{followerId}")
                .then()
                .statusCode(200)
                .body("blocked", equalTo(false));

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId1)
                .pathParam("followId", userId2)
                .when()
                .delete("/follower/remove-follower/currUser/{userId}/followUser/{followId}")
                .then()
                .statusCode(200);
    }

    @Test
    public void CreateUsersAndFollowersAndCheckCountsTest() {
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

        // Check follower count for followed user
        given()
                .pathParam("followerUserId", userId2)
                .when()
                .get("/follower/get-follower-count/{followerUserId}")
                .then()
                .statusCode(200)
                .body(equalTo("1"));

        // Check following count for following user
        given()
                .pathParam("followingUserId", userId1)
                .when()
                .get("/follower/get-following-count/{followingUserId}")
                .then()
                .statusCode(200)
                .body(equalTo("1"));
    }
}
