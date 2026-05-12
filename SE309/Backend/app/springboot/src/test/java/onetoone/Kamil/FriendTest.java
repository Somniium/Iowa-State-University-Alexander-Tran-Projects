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
public class FriendTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllFriendsTest() {
        given()
                .when()
                .get("/friends")
                .then()
                .statusCode(200);
    }

    @Test
    public void createFriendRequestAndPerformOperationsTest() {
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

        // POST a new friend request
        long reqId = ((Number) given()
                .pathParam("userId", userId1)
                .pathParam("friendId", userId2)
                .when()
                .post("/friend/send-request/currUser/{userId}/friend/{friendId}")
                .then()
                .statusCode(201)
                .extract()
                .path("id")).longValue();

        // GET request status
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId1)
                .pathParam("friendId", userId2)
                .when()
                .get("/friend/request-status/currUser/{userId}/friend/{friendId}").then()
                .statusCode(200);

        // ACCEPT request
        given()
                .contentType(ContentType.JSON)
                .pathParam("requestId", reqId)
                .when()
                .put("/friend/accept-request/{requestId}").then()
                .statusCode(200)
                .body("friendStatus", equalTo("ACCEPTED"));

        // DECLINE request
        given()
                .contentType(ContentType.JSON)
                .pathParam("requestId", reqId)
                .when()
                .put("/friend/decline-request/{requestId}").then()
                .statusCode(200)
                .body("friendStatus", equalTo("DECLINED"));

        // REMOVE / CANCEL request
        given()
                .contentType(ContentType.JSON)
                .pathParam("requestId", reqId)
                .when()
                .delete("/friend/remove-friend-or-cancel-request/{requestId}").then()
                .statusCode(200);
    }

    @Test
    public void CreateRequestCheckRequestsTest() {
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

        // POST a new friend request
        long reqId = ((Number) given()
                .pathParam("userId", userId1)
                .pathParam("friendId", userId2)
                .when()
                .post("/friend/send-request/currUser/{userId}/friend/{friendId}")
                .then()
                .extract()
                .path("id")).longValue();

        // Check requests sent by user
        given()
                .pathParam("userId", userId1)
                .when()
                .get("/friends/sent-by/{userId}")
                .then()
                .statusCode(200);

        // Check requests received by user
        given()
                .pathParam("userId", userId1)
                .when()
                .get("/friends/received-by/{userId}")
                .then()
                .statusCode(200);
    }
}
