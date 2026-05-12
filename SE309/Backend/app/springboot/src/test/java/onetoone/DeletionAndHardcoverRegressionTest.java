package onetoone;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import onetoone.Books.HardcoverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:regressiondb;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.show_sql=false",
        "logging.level.org=ERROR",
        "hardcover.api.url=http://localhost/unused",
        "hardcover.api.token=Bearer test-token"
})
public class DeletionAndHardcoverRegressionTest {

    @LocalServerPort
    int port;

    @MockBean
    private HardcoverService hardcoverService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    @Test
    public void deleteUserByIdRemovesFollowerReferencesFirst() {
        int userId = createUserAndReturnId("DeleteFollowA");
        int followedId = createUserAndReturnId("DeleteFollowB");

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
                .pathParam("followId", followedId)
                .when()
                .post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then()
                .statusCode(201);

        given()
                .pathParam("id", userId)
                .when()
                .delete("/users/id/{id}")
                .then()
                .statusCode(200)
                .body("message", equalTo("success"));

        given()
                .pathParam("id", userId)
                .when()
                .get("/users/id/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteProfessorByIdUnlinksOwnedGroupsFirst() {
        int userId = createUserAndReturnId("DeleteProfessor");

        long professorId = ((Number) given()
                .pathParam("id", userId)
                .when()
                .post("/professor/{id}")
                .then()
                .statusCode(200)
                .extract()
                .path("id")).longValue();

        given()
                .pathParam("id", professorId)
                .pathParam("className", "COMS309")
                .when()
                .put("/professor/add-class/{id}/{className}")
                .then()
                .statusCode(200);

        given()
                .pathParam("groupName", "DeleteProfessorGroup" + random.nextInt(1_000_000))
                .pathParam("profId", professorId)
                .pathParam("className", "COMS309")
                .when()
                .post("/group/name/{groupName}/professor/{profId}/class/{className}")
                .then()
                .statusCode(201);

        given()
                .pathParam("id", professorId)
                .when()
                .delete("/professor/remove-professor/id/{id}")
                .then()
                .statusCode(200)
                .body(containsString("Successfully removed professor."));

        given()
                .pathParam("id", professorId)
                .when()
                .get("/professor/id/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void hardcoverSearchFailureReturnsBadGateway() {
        when(hardcoverService.searchBooks(eq("dune"), eq(5)))
                .thenThrow(new RuntimeException("upstream unavailable"));

        given()
                .queryParam("q", "dune")
                .when()
                .get("/search-books")
                .then()
                .statusCode(502)
                .body("message", containsString("Hardcover search failed"));
    }

    @Test
    public void getProfilesReturnsSavedProfiles() {
        int userId = createUserAndReturnId("ListProfiles");

        String profileJson =
                "{" +
                        "\"bio\": \"Testing profile listing\"," +
                        "\"major\": \"Software Engineering\"," +
                        "\"hobbies\": \"Postman\"," +
                        "\"gradDate\": \"2026\"," +
                        "\"linkedInURL\": \"https://linkedin.com/in/postman\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(profileJson)
                .pathParam("id", userId)
                .when()
                .post("/profile/userId/{id}")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/profiles")
                .then()
                .statusCode(200)
                .body("find { it.profileId == " + userId + " }.bio", equalTo("Testing profile listing"));
    }

    @Test
    public void getProfilesIgnoresProfilesWhoseUserWasDeleted() {
        int userId = createUserAndReturnId("OrphanProfile");
        int orphanProfileId = userId + 100_000;

        String profileJson =
                "{" +
                        "\"bio\": \"Valid profile\"," +
                        "\"major\": \"Software Engineering\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(profileJson)
                .pathParam("id", userId)
                .when()
                .post("/profile/userId/{id}")
                .then()
                .statusCode(201);

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update(
                "INSERT INTO profile (user_id, bio, major) VALUES (?, ?, ?)",
                orphanProfileId,
                "Orphaned profile",
                "Missing user"
        );
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        given()
                .when()
                .get("/profiles")
                .then()
                .statusCode(200)
                .body("find { it.profileId == " + userId + " }.bio", equalTo("Valid profile"))
                .body("find { it.profileId == " + orphanProfileId + " }", equalTo(null));
    }

    @Test
    public void updateProfileOnlyChangesProvidedFields() {
        int userId = createUserAndReturnId("PatchProfile");

        String profileJson =
                "{" +
                        "\"bio\": \"Original bio\"," +
                        "\"major\": \"Software Engineering\"," +
                        "\"hobbies\": \"Reading\"," +
                        "\"gradDate\": \"2026\"," +
                        "\"linkedInURL\": \"https://linkedin.com/in/original\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(profileJson)
                .pathParam("id", userId)
                .when()
                .post("/profile/userId/{id}")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"bio\":\"Updated from Postman\"}")
                .pathParam("id", userId)
                .when()
                .put("/update-profile/{id}")
                .then()
                .statusCode(200)
                .body("bio", equalTo("Updated from Postman"))
                .body("major", equalTo("Software Engineering"))
                .body("hobbies", equalTo("Reading"))
                .body("gradDate", equalTo("2026"))
                .body("linkedInURL", equalTo("https://linkedin.com/in/original"));
    }

    private int createUserAndReturnId(String namePrefix) {
        int id = random.nextInt(1_000_000);
        String name = namePrefix + id;
        String email = namePrefix.toLowerCase() + id + "@iastate.edu";

        String userJson =
                "{" +
                        "\"name\": \"" + name + "\"," +
                        "\"emailId\": \"" + email + "\"," +
                        "\"password\": \"Hello45!\"" +
                        "}";

        given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("message", equalTo("success"));

        return given()
                .pathParam("email", email)
                .when()
                .get("/users/email/{email}")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }
}
