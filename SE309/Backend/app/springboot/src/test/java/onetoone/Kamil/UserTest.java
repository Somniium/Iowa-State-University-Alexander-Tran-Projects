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
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {

    @LocalServerPort
    int port;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void getAllUsersTest() {
        given()
                .when()
                .get("/users")
                .then()
                .statusCode(200);
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

        // GET the test user to see if it was saved (using email)
        given()
                .pathParam("email", email)
                .when()
                .get("/users/email/{email}")
                .then()
                .statusCode(200)
                .body("name", equalTo(name));
    }

    @Test
    public void registerUserAndGetGroupsTest() {
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


        // GET the test user to see if it was saved (and save ID)
        int userId = given()
                .pathParam("emailId", email)
                .when()
                .get("/users/email/{emailId}")
                .then()
                .extract()
                .path("id");

        // See if empty list of groups is returned
        given()
                .pathParam("id", userId)
                .when()
                .get("/get-user-groups/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(0));
    }

    @Test
    public void registerUserAndUpdate() {
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
        int userId = given()
                .pathParam("emailId", email)
                .when()
                .get("/users/email/{emailId}")
                .then()
                .extract()
                .path("id");

        // PUT to update user
        String updatedName = "KamilUpdate" + randId;
        String updateJSON = "{\"name\": \"" + updatedName + "\"}";

        given()
                .contentType(ContentType.JSON)
                .body(updateJSON)
                .pathParam("id", userId)
                .when()
                .put("/users/{id}")
                .then()
                .statusCode(200)
                .body("name", equalTo(updatedName))
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
    public void registerAndDeleteUserWithIDFromDBTest() {
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
        int userId = given()
                .pathParam("emailId", email)
                .when()
                .get("/users/email/{emailId}")
                .then()
                .extract()
                .path("id");

        //DEL the test user from the DB.

        given()
                .pathParam("id", userId)
                .when()
                .delete("/users/id/{id}")
                .then()
                .statusCode(200)
                .body("message", equalTo("success"));
    }
}
