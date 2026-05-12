package onetoone;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.Random;

/**
 * End-to-end system tests for the CyVal backend.
 *
 * Each test boots the full Spring Boot application on a random port, hits real
 * HTTP endpoints with RestAssured, and verifies the persisted side-effects via
 * follow-up calls.
 *
 * @author Alex Tran
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AlexSystemTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        // Endpoints like POST /users return text/plain JSON — register a parser
        // so RestAssured can still pull fields out of the body.
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    /**
     * Test 1: only @iastate.edu emails may register.
     *
     * The createUser endpoint silently returns 200 even on failure, but the
     * response body carries an error message. We verify the validation fires
     * by checking the message text, not just the HTTP code.
     */
    @Test
    public void registerWithNonIsuEmailIsRejectedTest() {
        Random rand = new Random();
        int randId = rand.nextInt(1_000_000);

        // Note: @gmail.com — should be rejected.
        String userJSON =
                "{" +
                        "\"name\": \"AlexNonIsu" + randId + "\"," +
                        "\"emailId\": \"alextest" + randId + "@gmail.com\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(userJSON)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("message", containsString("ISU email required"));
    }

    /**
     * Test 2: duplicate name and duplicate email are both rejected.
     *
     * Register a user, then try to register a second user with the same name
     * (different email) and a third with the same email (different name).
     * Each duplicate attempt must return a specific error message — proves the
     * createUser path actually checks both fields, not just one.
     */
    @Test
    public void duplicateUserRegistrationIsRejectedTest() {
        Random rand = new Random();
        int randId = rand.nextInt(1_000_000);

        String name = "AlexDup" + randId;
        String email = "alexdup" + randId + "@iastate.edu";

        String firstUserJSON =
                "{" +
                        "\"name\": \"" + name + "\"," +
                        "\"emailId\": \"" + email + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        // 1. Initial user — should succeed.
        given()
                .contentType(ContentType.JSON)
                .body(firstUserJSON)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("message", equalTo("success"));

        // 2. Same name, different email — must fail with the name-collision message.
        String dupNameJSON =
                "{" +
                        "\"name\": \"" + name + "\"," +
                        "\"emailId\": \"different" + randId + "@iastate.edu\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(dupNameJSON)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("message", containsString("name already exists"));

        // 3. Same email, different name — must fail with the email-collision message.
        String dupEmailJSON =
                "{" +
                        "\"name\": \"DifferentName" + randId + "\"," +
                        "\"emailId\": \"" + email + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(dupEmailJSON)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("message", containsString("email already exists"));
    }

    /**
     * Test 3: friend request creation and bidirectional status lookup.
     *
     * After A sends a request to B, the relationship must be discoverable from
     * either direction, querying the status as A→B or B→A should both return
     * "PENDING". Verifies the bidirectional JPQL query in FriendRepository.
     *
     * Exercises:
     *   POST /users (x2)
     *   GET  /users/email/{email}              (fetch generated IDs)
     *   POST /friend/send-request/...          (creates the request)
     *   GET  /friend/request-status/A/B        (asserts PENDING)
     *   GET  /friend/request-status/B/A        (asserts PENDING — bidirectional)
     */
    @Test
    public void friendRequestLifecycleTest() {
        Random rand = new Random();
        int randId1 = rand.nextInt(1_000_000);
        int randId2 = rand.nextInt(1_000_000);

        String email1 = "alexfriend" + randId1 + "@iastate.edu";
        String email2 = "alexfriend" + randId2 + "@iastate.edu";

        String userA =
                "{" +
                        "\"name\": \"AlexFriendA" + randId1 + "\"," +
                        "\"emailId\": \"" + email1 + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";
        String userB =
                "{" +
                        "\"name\": \"AlexFriendB" + randId2 + "\"," +
                        "\"emailId\": \"" + email2 + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        // Create both users.
        given().contentType(ContentType.JSON).body(userA).when().post("/users")
                .then().statusCode(200).body("message", equalTo("success"));
        given().contentType(ContentType.JSON).body(userB).when().post("/users")
                .then().statusCode(200).body("message", equalTo("success"));

        // Resolve their IDs.
        int idA = given().pathParam("emailId", email1)
                .when().get("/users/email/{emailId}")
                .then().statusCode(200).extract().path("id");
        int idB = given().pathParam("emailId", email2)
                .when().get("/users/email/{emailId}")
                .then().statusCode(200).extract().path("id");

        // A sends a friend request to B, must be created with status PENDING.
        // Extract the raw body and assert with plain JUnit to avoid any
        // JSON-parser-vs-text/plain confusion in RestAssured's body matchers.
        String createdJson = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", idA)
                .pathParam("friendId", idB)
                .when()
                .post("/friend/send-request/currUser/{userId}/friend/{friendId}")
                .then()
                .statusCode(201)
                .extract().asString();

        assertTrue("Expected response to contain PENDING but got: " + createdJson,
                createdJson.contains("\"friendStatus\":\"PENDING\""));

        // Query A→B direction — body is the literal string "PENDING" (text/plain).
        String statusAtoB = given()
                .pathParam("userId", idA)
                .pathParam("friendId", idB)
                .when()
                .get("/friend/request-status/currUser/{userId}/friend/{friendId}")
                .then()
                .statusCode(200)
                .extract().asString();

        assertEquals("PENDING", statusAtoB);

        // Query the SAME relationship from the reverse direction (B→A).
        // The repository's findFriendBetweenUsers query is bidirectional, so
        // this should resolve to the same record and also report PENDING.
        String statusBtoA = given()
                .pathParam("userId", idB)
                .pathParam("friendId", idA)
                .when()
                .get("/friend/request-status/currUser/{userId}/friend/{friendId}")
                .then()
                .statusCode(200)
                .extract().asString();

        assertEquals("PENDING", statusBtoA);
    }

    /**
     * Test 4: a follow relationship updates BOTH users' counts correctly.
     *
     * Both users start at 0 followers / 0 following. After A follows B:
     *   - A's "following" count must be 1
     *   - B's "follower"  count must be 1
     * Verifies the FK direction is wired correctly on both sides.
     */
    @Test
    public void followRelationshipUpdatesBothCountsTest() {
        Random rand = new Random();
        int randId1 = rand.nextInt(1_000_000);
        int randId2 = rand.nextInt(1_000_000);

        String email1 = "alexcount" + randId1 + "@iastate.edu";
        String email2 = "alexcount" + randId2 + "@iastate.edu";

        String userA =
                "{" +
                        "\"name\": \"AlexCountA" + randId1 + "\"," +
                        "\"emailId\": \"" + email1 + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";
        String userB =
                "{" +
                        "\"name\": \"AlexCountB" + randId2 + "\"," +
                        "\"emailId\": \"" + email2 + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        given().contentType(ContentType.JSON).body(userA).when().post("/users")
                .then().statusCode(200).body("message", equalTo("success"));
        given().contentType(ContentType.JSON).body(userB).when().post("/users")
                .then().statusCode(200).body("message", equalTo("success"));

        int idA = given().pathParam("emailId", email1)
                .when().get("/users/email/{emailId}")
                .then().statusCode(200).extract().path("id");
        int idB = given().pathParam("emailId", email2)
                .when().get("/users/email/{emailId}")
                .then().statusCode(200).extract().path("id");

        // Both new users start with zero followers and zero followings.
        // Counts come back as plain text — extract the body string and
        // assert with plain JUnit.
        assertEquals("0", given().pathParam("followerUserId", idA)
                .when().get("/follower/get-follower-count/{followerUserId}")
                .then().statusCode(200).extract().asString());
        assertEquals("0", given().pathParam("followingUserId", idA)
                .when().get("/follower/get-following-count/{followingUserId}")
                .then().statusCode(200).extract().asString());
        assertEquals("0", given().pathParam("followerUserId", idB)
                .when().get("/follower/get-follower-count/{followerUserId}")
                .then().statusCode(200).extract().asString());

        // A follows B.
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", idA)
                .pathParam("followId", idB)
                .when()
                .post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then()
                .statusCode(201);

        // A is now following 1 person.
        assertEquals("1", given().pathParam("followingUserId", idA)
                .when().get("/follower/get-following-count/{followingUserId}")
                .then().statusCode(200).extract().asString());

        // B now has 1 follower.
        assertEquals("1", given().pathParam("followerUserId", idB)
                .when().get("/follower/get-follower-count/{followerUserId}")
                .then().statusCode(200).extract().asString());

        // Sanity Check: A's follower count is still 0 (nobody follows A back).
        assertEquals("0", given().pathParam("followerUserId", idA)
                .when().get("/follower/get-follower-count/{followerUserId}")
                .then().statusCode(200).extract().asString());
    }
}
