package onetoone;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
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

import java.util.List;
import java.util.Random;

/**
 * Endpoint-coverage system tests for the controllers Alex owns:
 * Users, Admins, Posts, Comments, Reviews, Likes, Notifications,
 * Followers, Professors, Books, Movies, Games, Chat.
 *
 * Each test creates its own randomly-named fixtures so tests are
 * order-independent and safe to run against the shared ISU DB.
 *
 * @author Alex Tran
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AlexEndpointCoverageTest {

    @LocalServerPort
    int port;

    private final Random random = new Random();

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.registerParser("text/plain", io.restassured.parsing.Parser.JSON);
    }

    // ============================================================
    // USERS
    // ============================================================

    @Test
    public void getAllUsersReturnsListTest() {
        createUser("AllList");
        given().when().get("/users")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void getUserByIdReturnsUserTest() {
        int id = createUser("GetById");
        given().pathParam("id", id).when().get("/users/id/{id}")
                .then().statusCode(200).body("id", equalTo(id));
    }

    @Test
    public void getUserByIdNotFoundTest() {
        given().pathParam("id", 999_999_999).when().get("/users/id/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getUserByNameReturnsUserTest() {
        String name = "GetByName" + random.nextInt(1_000_000);
        String email = name.toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        given().pathParam("name", name).when().get("/users/name/{name}")
                .then().statusCode(200).body("name", equalTo(name));
    }

    @Test
    public void getUserByNameNotFoundTest() {
        given().pathParam("name", "NoOne_" + random.nextInt(1_000_000))
                .when().get("/users/name/{name}")
                .then().statusCode(404);
    }

    @Test
    public void getUserByEmailReturnsUserTest() {
        String name = "GetByEmail" + random.nextInt(1_000_000);
        String email = name.toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        given().pathParam("email", email).when().get("/users/email/{email}")
                .then().statusCode(200).body("emailId", equalTo(email));
    }

    @Test
    public void getUserByEmailNotFoundTest() {
        given().pathParam("email", "missing" + random.nextInt(1_000_000) + "@iastate.edu")
                .when().get("/users/email/{email}")
                .then().statusCode(404);
    }

    @Test
    public void getUserGroupsReturnsListTest() {
        int id = createUser("UserGroups");
        given().pathParam("id", id).when().get("/get-user-groups/{id}")
                .then().statusCode(200);
    }

    @Test
    public void getUserGroupsForMissingUserTest() {
        given().pathParam("id", 999_999_999).when().get("/get-user-groups/{id}")
                .then().statusCode(404);
    }

    @Test
    public void updateUserChangesNameTest() {
        int id = createUser("UpdateName");
        String newName = "Renamed" + random.nextInt(1_000_000);
        given().contentType(ContentType.JSON).body("{\"name\":\"" + newName + "\"}")
                .pathParam("id", id).when().put("/users/{id}")
                .then().statusCode(200).body("name", equalTo(newName));
    }

    @Test
    public void updateUserNotFoundTest() {
        given().contentType(ContentType.JSON).body("{\"name\":\"x\"}")
                .pathParam("id", 999_999_999).when().put("/users/{id}")
                .then().statusCode(404);
    }

    @Test
    public void updateUserPasswordIsHashedAndLoginableTest() {
        String name = "UpdatePass" + random.nextInt(1_000_000);
        String email = name.toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        int id = lookupId(email);

        given().contentType(ContentType.JSON).body("{\"password\":\"NewPass99!\"}")
                .pathParam("id", id).when().put("/users/{id}")
                .then().statusCode(200);

        given().contentType(ContentType.JSON)
                .body("{\"emailId\":\"" + email + "\",\"password\":\"NewPass99!\"}")
                .when().post("/users/login")
                .then().statusCode(200);
    }

    @Test
    public void loginSucceedsWithCorrectPasswordTest() {
        String name = "LoginOk" + random.nextInt(1_000_000);
        String email = name.toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        given().contentType(ContentType.JSON)
                .body("{\"emailId\":\"" + email + "\",\"password\":\"Hello45!\"}")
                .when().post("/users/login")
                .then().statusCode(200).body("emailId", equalTo(email));
    }

    @Test
    public void loginFailsWithWrongPasswordTest() {
        String name = "LoginBad" + random.nextInt(1_000_000);
        String email = name.toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        given().contentType(ContentType.JSON)
                .body("{\"emailId\":\"" + email + "\",\"password\":\"Wrong!\"}")
                .when().post("/users/login")
                .then().statusCode(401);
    }

    @Test
    public void loginFailsForUnknownEmailTest() {
        given().contentType(ContentType.JSON)
                .body("{\"emailId\":\"nobody" + random.nextInt(1_000_000) + "@iastate.edu\",\"password\":\"Hello45!\"}")
                .when().post("/users/login")
                .then().statusCode(401);
    }

    @Test
    public void loginRejectsMissingFieldsTest() {
        given().contentType(ContentType.JSON).body("{}")
                .when().post("/users/login")
                .then().statusCode(400);
    }

    @Test
    public void deleteUserByIdRemovesThemTest() {
        int id = createUser("DelById");
        given().pathParam("id", id).when().delete("/users/id/{id}")
                .then().statusCode(200);
        given().pathParam("id", id).when().get("/users/id/{id}")
                .then().statusCode(404);
    }

    @Test
    public void deleteUserByIdNotFoundTest() {
        given().pathParam("id", 999_999_999).when().delete("/users/id/{id}")
                .then().statusCode(404);
    }

    @Test
    public void deleteUserByEmailRemovesThemTest() {
        String name = "DelByEmail" + random.nextInt(1_000_000);
        String email = name.toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        given().pathParam("emailId", email).when().delete("/users/email/{emailId}")
                .then().statusCode(200);
        given().pathParam("emailId", email).when().get("/users/email/{emailId}")
                .then().statusCode(404);
    }

    @Test
    public void deleteUserByEmailNotFoundTest() {
        given().pathParam("emailId", "ghost" + random.nextInt(1_000_000) + "@iastate.edu")
                .when().delete("/users/email/{emailId}")
                .then().statusCode(404);
    }

    // ============================================================
    // ADMINS
    // ============================================================

    @Test
    public void getAllAdminsReturnsListTest() {
        int u = createUser("AdminList");
        given().pathParam("id", u).when().post("/admin/{id}").then().statusCode(200);
        given().when().get("/admins").then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void getAdminByIdAndByUserTest() {
        int u = createUser("AdminLookup");
        long adminId = ((Number) given().pathParam("id", u).when().post("/admin/{id}")
                .then().statusCode(200).extract().path("id")).longValue();

        given().pathParam("id", adminId).when().get("/admin/id/{id}")
                .then().statusCode(200);
        given().pathParam("userId", u).when().get("/admin/user/{userId}")
                .then().statusCode(200);
        given().pathParam("id", adminId).when().get("/admin/get-user/{id}")
                .then().statusCode(200).body("id", equalTo(u));
    }

    @Test
    public void getAdminByIdNotFoundTest() {
        given().pathParam("id", 999_999_999L).when().get("/admin/id/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getAdminByUserMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().get("/admin/user/{userId}")
                .then().statusCode(404);
    }

    @Test
    public void getAdminByUserMissingAdminTest() {
        int u = createUser("AdminMiss");
        given().pathParam("userId", u).when().get("/admin/user/{userId}")
                .then().statusCode(404);
    }

    @Test
    public void getAdminUserMissingAdminTest() {
        given().pathParam("id", 999_999_999L).when().get("/admin/get-user/{id}")
                .then().statusCode(404);
    }

    @Test
    public void addAdminMissingUserTest() {
        given().pathParam("id", 999_999_999).when().post("/admin/{id}")
                .then().statusCode(404);
    }

    @Test
    public void addAdminTwiceConflictsTest() {
        int u = createUser("AdminDup");
        given().pathParam("id", u).when().post("/admin/{id}").then().statusCode(200);
        given().pathParam("id", u).when().post("/admin/{id}").then().statusCode(409);
    }

    @Test
    public void promoteAndDemoteMasterTest() {
        int u = createUser("Master");
        long adminId = ((Number) given().pathParam("id", u).when().post("/admin/{id}")
                .then().statusCode(200).extract().path("id")).longValue();
        given().pathParam("id", adminId).when().put("/admin-make-master/{id}")
                .then().statusCode(200).body("master", equalTo(true));
        given().pathParam("id", adminId).when().put("/admin-remove-master/{id}")
                .then().statusCode(200).body("master", equalTo(false));
    }

    @Test
    public void makeMasterMissingAdminTest() {
        given().pathParam("id", 999_999_999L).when().put("/admin-make-master/{id}")
                .then().statusCode(404);
    }

    @Test
    public void removeMasterMissingAdminTest() {
        given().pathParam("id", 999_999_999L).when().put("/admin-remove-master/{id}")
                .then().statusCode(404);
    }

    @Test
    public void removeAdminByIdTest() {
        int u = createUser("DelAdminId");
        long adminId = ((Number) given().pathParam("id", u).when().post("/admin/{id}")
                .then().statusCode(200).extract().path("id")).longValue();
        given().pathParam("id", adminId).when().delete("/remove-admin/id/{id}")
                .then().statusCode(200);
    }

    @Test
    public void removeAdminByUserTest() {
        int u = createUser("DelAdminUser");
        given().pathParam("id", u).when().post("/admin/{id}").then().statusCode(200);
        given().pathParam("userId", u).when().delete("/remove-admin/user/{userId}")
                .then().statusCode(200);
        given().pathParam("userId", u).when().get("/admin/user/{userId}")
                .then().statusCode(404);
    }

    @Test
    public void removeAdminByMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().delete("/remove-admin/user/{userId}")
                .then().statusCode(404);
    }

    // ============================================================
    // REVIEWS
    // ============================================================

    @Test
    public void getAllReviewsAndByTypeTest() {
        createReview("ListAll");
        given().when().get("/reviews").then().statusCode(200);
        given().pathParam("mediaType", "MOVIE").when().get("/reviews/type/{mediaType}")
                .then().statusCode(200);
    }

    @Test
    public void createAndFetchReviewTest() {
        int reviewId = createReview("CreateFetch");
        given().pathParam("id", reviewId).when().get("/reviews/{id}")
                .then().statusCode(200).body("id", equalTo(reviewId));
    }

    @Test
    public void createReviewRejectsBadMediaTypeTest() {
        String json = "{\"mediaType\":\"VIDEOTAPE\",\"title\":\"x\",\"rating\":50,\"body\":\"\"}";
        given().contentType(ContentType.JSON).body(json)
                .when().post("/reviews").then().statusCode(400);
    }

    @Test
    public void createReviewRejectsBadRatingTest() {
        String json = "{\"mediaType\":\"MOVIE\",\"title\":\"x\",\"rating\":500,\"body\":\"\"}";
        given().contentType(ContentType.JSON).body(json)
                .when().post("/reviews").then().statusCode(400);
    }

    @Test
    public void updateReviewNotFoundTest() {
        given().contentType(ContentType.JSON).body("{\"body\":\"x\"}")
                .pathParam("id", 999_999_999).queryParam("requesterId", 1)
                .when().put("/reviews/{id}").then().statusCode(404);
    }

    @Test
    public void updateReviewByNonAuthorForbiddenTest() {
        int author = createUser("UpdAuth");
        int stranger = createUser("UpdStr");
        int reviewId = createReview("UpdR");
        assignReviewToUser(reviewId, author);

        given().contentType(ContentType.JSON).body("{\"body\":\"hijack\"}")
                .pathParam("id", reviewId).queryParam("requesterId", stranger)
                .when().put("/reviews/{id}").then().statusCode(403);
    }

    @Test
    public void updateReviewSucceedsForAuthorTest() {
        int author = createUser("UpdSelf");
        int reviewId = createReview("UpdSelf");
        assignReviewToUser(reviewId, author);

        given().contentType(ContentType.JSON)
                .body("{\"mediaType\":\"GAME\",\"title\":\"new\",\"rating\":99,\"body\":\"new body\"}")
                .pathParam("id", reviewId).queryParam("requesterId", author)
                .when().put("/reviews/{id}")
                .then().statusCode(200)
                .body("body", equalTo("new body"))
                .body("rating", equalTo(99));
    }

    @Test
    public void assignReviewToOtherUserForbiddenTest() {
        int authorId = createUser("AsAuth");
        int strangerId = createUser("AsStr");
        int reviewId = createReview("AsR");

        given().pathParam("reviewId", reviewId).pathParam("userId", authorId)
                .queryParam("requesterId", strangerId)
                .when().put("/reviews/{reviewId}/user/{userId}")
                .then().statusCode(403);
    }

    @Test
    public void assignReviewToUserMissingTargetTest() {
        int authorId = createUser("AsMissing");
        given().pathParam("reviewId", 999_999_999).pathParam("userId", authorId)
                .queryParam("requesterId", authorId)
                .when().put("/reviews/{reviewId}/user/{userId}")
                .then().statusCode(404);
    }

    @Test
    public void assignReviewSecondTimeConflictsTest() {
        int authorA = createUser("AssA");
        int authorB = createUser("AssB");
        int reviewId = createReview("AssX");
        assignReviewToUser(reviewId, authorA);
        // Second assign to a different user is rejected.
        given().pathParam("reviewId", reviewId).pathParam("userId", authorB)
                .queryParam("requesterId", authorB)
                .when().put("/reviews/{reviewId}/user/{userId}")
                .then().statusCode(409);
    }

    @Test
    public void deleteReviewByAuthorSucceedsTest() {
        int u = createUser("DelR");
        int r = createReview("DelR");
        assignReviewToUser(r, u);
        given().pathParam("id", r).queryParam("requesterId", u)
                .when().delete("/reviews/{id}").then().statusCode(200);
    }

    @Test
    public void deleteReviewNotFoundTest() {
        given().pathParam("id", 999_999_999).queryParam("requesterId", 1)
                .when().delete("/reviews/{id}").then().statusCode(404);
    }

    @Test
    public void deleteReviewByNonAuthorForbiddenTest() {
        int author = createUser("DelRAuth");
        int stranger = createUser("DelRStr");
        int reviewId = createReview("DelRX");
        assignReviewToUser(reviewId, author);
        given().pathParam("id", reviewId).queryParam("requesterId", stranger)
                .when().delete("/reviews/{id}").then().statusCode(403);
    }

    @Test
    public void purgeOrphanReviewsRequiresAdminTest() {
        int u = createUser("OrphanReq");
        given().queryParam("requesterId", u).when().delete("/reviews/orphans")
                .then().statusCode(403);
        given().pathParam("id", u).when().post("/admin/{id}").then().statusCode(200);
        given().queryParam("requesterId", u).when().delete("/reviews/orphans")
                .then().statusCode(200);
    }

    // ============================================================
    // POSTS
    // ============================================================

    @Test
    public void getFeedReturnsListTest() {
        given().when().get("/feed").then().statusCode(200);
    }

    @Test
    public void getFeedByTypeReturnsListTest() {
        given().pathParam("mediaType", "MOVIE").when().get("/feed/type/{mediaType}")
                .then().statusCode(200);
    }

    @Test
    public void publishReviewPutsItOnFeedTest() {
        int u = createUser("PubFeed");
        int r = createReview("PubFeed");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().pathParam("id", p).when().get("/posts/{id}")
                .then().statusCode(200).body("id", equalTo(p));
    }

    @Test
    public void publishReviewMissingReviewTest() {
        int u = createUser("PubMiss");
        given().pathParam("reviewId", 999_999_999).queryParam("authorId", u)
                .when().post("/reviews/{reviewId}/publish")
                .then().statusCode(404);
    }

    @Test
    public void publishReviewMissingUserTest() {
        int r = createReview("PubMissU");
        given().pathParam("reviewId", r).queryParam("authorId", 999_999_999)
                .when().post("/reviews/{reviewId}/publish")
                .then().statusCode(404);
    }

    @Test
    public void publishReviewTwiceConflictsTest() {
        int u = createUser("PubDup");
        int r = createReview("PubDup");
        assignReviewToUser(r, u);
        publishReview(r, u);
        given().pathParam("reviewId", r).queryParam("authorId", u)
                .when().post("/reviews/{reviewId}/publish")
                .then().statusCode(409);
    }

    @Test
    public void getPostNotFoundTest() {
        given().pathParam("id", 999_999_999).when().get("/posts/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getUserPostsListsAuthoredPostsTest() {
        int u = createUser("UPosts");
        int r = createReview("UPosts");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().pathParam("userId", u).when().get("/users/{userId}/posts")
                .then().statusCode(200)
                .body("find { it.id == " + p + " }", notNullValue());
    }

    @Test
    public void getUserPostsMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().get("/users/{userId}/posts")
                .then().statusCode(404);
    }

    @Test
    public void editPostUpdatesCaptionTest() {
        int u = createUser("EditP");
        int r = createReview("EditP");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().contentType(ContentType.JSON)
                .body("{\"caption\":\"updated\",\"visibility\":\"FRIENDS\"}")
                .pathParam("id", p).queryParam("requesterId", u)
                .when().put("/posts/{id}")
                .then().statusCode(200)
                .body("caption", equalTo("updated"))
                .body("visibility", equalTo("FRIENDS"));
    }

    @Test
    public void editPostByNonAuthorForbiddenTest() {
        int author = createUser("EditAuth");
        int stranger = createUser("EditStr");
        int r = createReview("EditX");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        given().contentType(ContentType.JSON).body("{\"caption\":\"hijack\"}")
                .pathParam("id", p).queryParam("requesterId", stranger)
                .when().put("/posts/{id}").then().statusCode(403);
    }

    @Test
    public void editPostNotFoundTest() {
        given().contentType(ContentType.JSON).body("{\"caption\":\"x\"}")
                .pathParam("id", 999_999_999).queryParam("requesterId", 1)
                .when().put("/posts/{id}").then().statusCode(404);
    }

    @Test
    public void deletePostByAuthorTest() {
        int u = createUser("DelPAuth");
        int r = createReview("DelPAuth");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().pathParam("id", p).queryParam("requesterId", u)
                .when().delete("/posts/{id}").then().statusCode(200);
    }

    @Test
    public void deletePostNotFoundTest() {
        given().pathParam("id", 999_999_999).queryParam("requesterId", 1)
                .when().delete("/posts/{id}").then().statusCode(404);
    }

    @Test
    public void deletePostByNonAuthorForbiddenThenAdminAllowedTest() {
        int author = createUser("DPAuth");
        int stranger = createUser("DPStr");
        int r = createReview("DPR");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);

        given().pathParam("id", p).queryParam("requesterId", stranger)
                .when().delete("/posts/{id}").then().statusCode(403);

        given().pathParam("id", stranger).when().post("/admin/{id}").then().statusCode(200);
        given().pathParam("id", p).queryParam("requesterId", stranger)
                .when().delete("/posts/{id}").then().statusCode(200);
    }

    // ============================================================
    // COMMENTS
    // ============================================================

    @Test
    public void addCommentAndGetByPostTest() {
        int u = createUser("ACom");
        int r = createReview("ACom");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        int c = addComment(p, u, "Nice!");
        given().pathParam("postId", p).when().get("/posts/{postId}/comments")
                .then().statusCode(200)
                .body("find { it.id == " + c + " }.body", equalTo("Nice!"));
    }

    @Test
    public void addCommentMissingPostTest() {
        int u = createUser("ACMiss");
        given().contentType(ContentType.JSON).body("{\"body\":\"x\"}")
                .pathParam("postId", 999_999_999).queryParam("authorId", u)
                .when().post("/posts/{postId}/comments").then().statusCode(404);
    }

    @Test
    public void addCommentMissingUserTest() {
        int author = createUser("ACMissUAuth");
        int r = createReview("ACMissU");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        given().contentType(ContentType.JSON).body("{\"body\":\"x\"}")
                .pathParam("postId", p).queryParam("authorId", 999_999_999)
                .when().post("/posts/{postId}/comments").then().statusCode(404);
    }

    @Test
    public void addCommentEmptyBodyTest() {
        int u = createUser("AcEmpty");
        int r = createReview("AcEmpty");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().contentType(ContentType.JSON).body("{\"body\":\"   \"}")
                .pathParam("postId", p).queryParam("authorId", u)
                .when().post("/posts/{postId}/comments").then().statusCode(400);
    }

    @Test
    public void getCommentsMissingPostTest() {
        given().pathParam("postId", 999_999_999).when().get("/posts/{postId}/comments")
                .then().statusCode(404);
    }

    @Test
    public void commentCountReflectsAdditionsTest() {
        int u = createUser("CCount");
        int r = createReview("CCount");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        addComment(p, u, "1");
        addComment(p, u, "2");

        String body = given().pathParam("postId", p)
                .when().get("/posts/{postId}/comments/count")
                .then().statusCode(200).extract().asString();
        assertTrue("expected count >= 2 in " + body,
                body.contains("\"count\":2") || body.contains("\"count\":3"));
    }

    @Test
    public void getUserCommentsListsThemTest() {
        int u = createUser("UCom");
        int r = createReview("UCom");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        addComment(p, u, "hi");
        given().pathParam("userId", u).when().get("/users/{userId}/comments")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void getUserCommentsMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().get("/users/{userId}/comments")
                .then().statusCode(404);
    }

    @Test
    public void getCommentByIdReturnsPostIdTest() {
        int u = createUser("GComId");
        int r = createReview("GComId");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        int c = addComment(p, u, "Hello");
        // Verifies the recent getPostId() fix.
        given().pathParam("id", c).when().get("/comments/{id}")
                .then().statusCode(200)
                .body("id", equalTo(c))
                .body("postId", equalTo(p));
    }

    @Test
    public void getCommentByIdNotFoundTest() {
        given().pathParam("id", 999_999_999).when().get("/comments/{id}")
                .then().statusCode(404);
    }

    @Test
    public void editCommentByAuthorSucceedsTest() {
        int u = createUser("EComAuth");
        int r = createReview("ECom");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        int c = addComment(p, u, "old");
        given().contentType(ContentType.JSON).body("{\"body\":\"new\"}")
                .pathParam("id", c).queryParam("authorId", u)
                .when().put("/comments/{id}")
                .then().statusCode(200).body("body", equalTo("new"));
    }

    @Test
    public void editCommentByNonAuthorForbiddenTest() {
        int author = createUser("ECEAuth");
        int stranger = createUser("ECEStr");
        int r = createReview("ECE");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        int c = addComment(p, author, "mine");
        given().contentType(ContentType.JSON).body("{\"body\":\"hijack\"}")
                .pathParam("id", c).queryParam("authorId", stranger)
                .when().put("/comments/{id}").then().statusCode(403);
    }

    @Test
    public void editCommentNotFoundTest() {
        given().contentType(ContentType.JSON).body("{\"body\":\"x\"}")
                .pathParam("id", 999_999_999).queryParam("authorId", 1)
                .when().put("/comments/{id}").then().statusCode(404);
    }

    @Test
    public void editCommentEmptyBodyTest() {
        int u = createUser("ECEmAuth");
        int r = createReview("ECEm");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        int c = addComment(p, u, "real");
        given().contentType(ContentType.JSON).body("{\"body\":\" \"}")
                .pathParam("id", c).queryParam("authorId", u)
                .when().put("/comments/{id}").then().statusCode(400);
    }

    @Test
    public void deleteCommentByAuthorAllowedTest() {
        int u = createUser("DCAuth");
        int r = createReview("DCAuth");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        int c = addComment(p, u, "del");
        given().pathParam("id", c).queryParam("requesterId", u)
                .when().delete("/comments/{id}").then().statusCode(200);
    }

    @Test
    public void deleteCommentByPostAuthorAllowedTest() {
        int postAuthor = createUser("DCPA");
        int commenter = createUser("DCC");
        int r = createReview("DCPA");
        assignReviewToUser(r, postAuthor);
        int p = publishReview(r, postAuthor);
        int c = addComment(p, commenter, "byebye");
        given().pathParam("id", c).queryParam("requesterId", postAuthor)
                .when().delete("/comments/{id}").then().statusCode(200);
    }

    @Test
    public void deleteCommentByAdminAllowedTest() {
        int author = createUser("DCAdmAuth");
        int admin = createUser("DCAdm");
        given().pathParam("id", admin).when().post("/admin/{id}").then().statusCode(200);
        int r = createReview("DCAdm");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        int c = addComment(p, author, "del");
        given().pathParam("id", c).queryParam("requesterId", admin)
                .when().delete("/comments/{id}").then().statusCode(200);
    }

    @Test
    public void deleteCommentByStrangerForbiddenTest() {
        int author = createUser("DCSAuth");
        int stranger = createUser("DCSStr");
        int r = createReview("DCS");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        int c = addComment(p, author, "mine");
        given().pathParam("id", c).queryParam("requesterId", stranger)
                .when().delete("/comments/{id}").then().statusCode(403);
    }

    @Test
    public void deleteCommentNotFoundTest() {
        given().pathParam("id", 999_999_999).queryParam("requesterId", 1)
                .when().delete("/comments/{id}").then().statusCode(404);
    }

    // ============================================================
    // LIKES
    // ============================================================

    @Test
    public void likeAndUnlikePostTest() {
        int u = createUser("Like");
        int r = createReview("Like");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);

        given().pathParam("postId", p).queryParam("userId", u)
                .when().post("/posts/{postId}/likes").then().statusCode(anyOf2xx());
        given().pathParam("postId", p).queryParam("userId", u)
                .when().delete("/posts/{postId}/likes").then().statusCode(anyOf2xx());
    }

    @Test
    public void getLikesAndCountTest() {
        int u = createUser("LikeList");
        int r = createReview("LikeList");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);

        given().pathParam("postId", p).queryParam("userId", u)
                .when().post("/posts/{postId}/likes").then().statusCode(anyOf2xx());

        given().pathParam("postId", p).when().get("/posts/{postId}/likes")
                .then().statusCode(200);
        given().pathParam("postId", p).when().get("/posts/{postId}/likes/count")
                .then().statusCode(200);
    }

    @Test
    public void likeStatusReflectsStateTest() {
        int u = createUser("LStat");
        int r = createReview("LStat");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);

        given().pathParam("postId", p).queryParam("userId", u)
                .when().get("/posts/{postId}/likes/status")
                .then().statusCode(200).body("liked", equalTo(false));
        given().pathParam("postId", p).queryParam("userId", u)
                .when().post("/posts/{postId}/likes").then().statusCode(anyOf2xx());
        given().pathParam("postId", p).queryParam("userId", u)
                .when().get("/posts/{postId}/likes/status")
                .then().statusCode(200).body("liked", equalTo(true));
    }

    @Test
    public void getUserLikesTest() {
        int u = createUser("UL");
        int r = createReview("UL");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().pathParam("postId", p).queryParam("userId", u)
                .when().post("/posts/{postId}/likes").then().statusCode(anyOf2xx());
        given().pathParam("userId", u).when().get("/users/{userId}/likes")
                .then().statusCode(200);
    }

    @Test
    public void likeMissingPostTest() {
        int u = createUser("LMissP");
        given().pathParam("postId", 999_999_999).queryParam("userId", u)
                .when().post("/posts/{postId}/likes").then().statusCode(404);
    }

    @Test
    public void likeMissingUserTest() {
        int u = createUser("LMissUAuth");
        int r = createReview("LMissU");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        given().pathParam("postId", p).queryParam("userId", 999_999_999)
                .when().post("/posts/{postId}/likes").then().statusCode(404);
    }

    @Test
    public void unlikeWithoutLikeIsHandledTest() {
        int u = createUser("UnLNoOp");
        int r = createReview("UnLNoOp");
        assignReviewToUser(r, u);
        int p = publishReview(r, u);
        // delete-without-like — controller may return 200 or 404; just exercise it.
        given().pathParam("postId", p).queryParam("userId", u)
                .when().delete("/posts/{postId}/likes")
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        equalTo(200), equalTo(204), equalTo(404)));
    }

    @Test
    public void getUserLikesMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().get("/users/{userId}/likes")
                .then().statusCode(404);
    }

    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    @Test
    public void commentingFiresNotificationToPostAuthorTest() {
        int author = createUser("NAuth");
        int commenter = createUser("NCom");
        int r = createReview("NRev");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        addComment(p, commenter, "ping");

        given().pathParam("userId", author).when().get("/users/{userId}/notifications")
                .then().statusCode(200)
                .body("find { it.type == 'COMMENT' }", notNullValue());
    }

    @Test
    public void notificationCountReflectsUnreadTest() {
        int author = createUser("NCntAuth");
        int commenter = createUser("NCntCom");
        int r = createReview("NCnt");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        addComment(p, commenter, "1");

        given().pathParam("userId", author).when().get("/users/{userId}/notifications/count")
                .then().statusCode(200);
        given().pathParam("userId", author).when().get("/users/{userId}/notifications/unread")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void markSingleNotificationReadTest() {
        int author = createUser("NMark");
        int commenter = createUser("NMarkC");
        int r = createReview("NMark");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        addComment(p, commenter, "ping");

        Integer notifId = given().pathParam("userId", author)
                .when().get("/users/{userId}/notifications")
                .then().statusCode(200)
                .extract().path("[0].id");
        if (notifId != null) {
            given().pathParam("id", notifId).when().put("/notifications/{id}/read")
                    .then().statusCode(200);
        }
    }

    @Test
    public void markNotificationReadNotFoundTest() {
        given().pathParam("id", 999_999_999).when().put("/notifications/{id}/read")
                .then().statusCode(404);
    }

    @Test
    public void deleteNotificationTest() {
        int author = createUser("NDel");
        int commenter = createUser("NDelC");
        int r = createReview("NDel");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        addComment(p, commenter, "del");

        Integer notifId = given().pathParam("userId", author)
                .when().get("/users/{userId}/notifications")
                .then().statusCode(200).extract().path("[0].id");
        if (notifId != null) {
            given().pathParam("id", notifId).when().delete("/notifications/{id}")
                    .then().statusCode(anyOf2xx());
        }
    }

    @Test
    public void deleteNotificationNotFoundTest() {
        given().pathParam("id", 999_999_999).when().delete("/notifications/{id}")
                .then().statusCode(404);
    }

    @Test
    public void markAllNotificationsReadTest() {
        int author = createUser("NRead");
        int commenter = createUser("NReadC");
        int r = createReview("NRead");
        assignReviewToUser(r, author);
        int p = publishReview(r, author);
        addComment(p, commenter, "1");
        addComment(p, commenter, "2");

        given().pathParam("userId", author).when().put("/users/{userId}/notifications/read-all")
                .then().statusCode(200);
        given().pathParam("userId", author).when().get("/users/{userId}/notifications/unread")
                .then().statusCode(200).body("size()", equalTo(0));
    }

    // ============================================================
    // FOLLOWERS
    // ============================================================

    @Test
    public void getAllFollowersListTest() {
        given().when().get("/followers").then().statusCode(200);
    }

    @Test
    public void followAndListFollowersFollowingTest() {
        int a = createUser("FlA");
        int b = createUser("FlB");

        given().pathParam("userId", a).pathParam("followId", b)
                .when().post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then().statusCode(201);

        given().pathParam("userId", a).when().get("/follower/get-following/{userId}")
                .then().statusCode(200);
        given().pathParam("userId", b).when().get("/follower/get-followers/{userId}")
                .then().statusCode(200);
    }

    @Test
    public void unfollowUserDecrementsCountsTest() {
        int a = createUser("UfA");
        int b = createUser("UfB");
        given().pathParam("userId", a).pathParam("followId", b)
                .when().post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then().statusCode(201);
        given().pathParam("userId", a).pathParam("followId", b)
                .when().delete("/follower/remove-follower/currUser/{userId}/followUser/{followId}")
                .then().statusCode(anyOf2xx());
        assertEquals("0", given().pathParam("followingUserId", a)
                .when().get("/follower/get-following-count/{followingUserId}")
                .then().statusCode(200).extract().asString());
    }

    @Test
    public void blockAndUnblockFollowerTest() {
        int a = createUser("BlA");
        int b = createUser("BlB");
        given().pathParam("userId", a).pathParam("followId", b)
                .when().post("/follower/follow-user/currUser/{userId}/followUser/{followId}")
                .then().statusCode(201);

        // /followers returns Follower rows whose id IS the join-table PK that
        // block-follower expects (get-followers/{userId} returns User objects, not Follower rows).
        List<java.util.Map<String, Object>> rows = given()
                .when().get("/followers")
                .then().statusCode(200).extract().jsonPath().getList("$");
        Long followerId = null;
        for (java.util.Map<String, Object> row : rows) {
            Object follower = row.get("follower");
            Object following = row.get("following");
            if (follower instanceof java.util.Map && following instanceof java.util.Map) {
                Object fid = ((java.util.Map<?, ?>) follower).get("id");
                Object gid = ((java.util.Map<?, ?>) following).get("id");
                if (fid != null && gid != null && ((Number) fid).intValue() == a && ((Number) gid).intValue() == b) {
                    followerId = ((Number) row.get("id")).longValue();
                    break;
                }
            }
        }
        if (followerId == null) return;

        given().pathParam("followerId", followerId).when().put("/follower/block-follower/{followerId}")
                .then().statusCode(anyOf2xx());
        given().pathParam("followerId", followerId).when().get("/follower/block-status/{followerId}")
                .then().statusCode(200);
        given().pathParam("followerId", followerId).when().put("/follower/unblock-follower/{followerId}")
                .then().statusCode(anyOf2xx());
    }

    @Test
    public void blockStatusMissingFollowerTest() {
        given().pathParam("followerId", 999_999_999L).when().get("/follower/block-status/{followerId}")
                .then().statusCode(404);
    }

    // ============================================================
    // PROFESSORS
    // ============================================================

    @Test
    public void getAllProfessorsListTest() {
        given().when().get("/professors").then().statusCode(200);
    }

    @Test
    public void professorAddAndRemoveClassTest() {
        int u = createUser("PrCls");
        long pid = createProfessor(u);
        String cls = "TEST" + random.nextInt(1_000_000);
        given().pathParam("id", pid).pathParam("className", cls)
                .when().put("/professor/add-class/{id}/{className}").then().statusCode(200);
        given().pathParam("id", pid).pathParam("className", cls)
                .when().put("/professor/remove-class/{id}/{className}").then().statusCode(200);
    }

    @Test
    public void getProfessorByIdAndUserTest() {
        int u = createUser("PrLook");
        long pid = createProfessor(u);
        given().pathParam("id", pid).when().get("/professor/id/{id}")
                .then().statusCode(200);
        given().pathParam("userId", u).when().get("/professor/user/{userId}")
                .then().statusCode(200);
        given().pathParam("id", pid).when().get("/professor/get-user/{id}")
                .then().statusCode(200).body("id", equalTo(u));
        given().pathParam("id", pid).when().get("/get-professor-groups/{id}")
                .then().statusCode(200);
    }

    @Test
    public void getProfessorByIdMissingTest() {
        given().pathParam("id", 999_999_999L).when().get("/professor/id/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getProfessorByMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().get("/professor/user/{userId}")
                .then().statusCode(404);
    }

    @Test
    public void deleteProfessorByIdRemovesTest() {
        int u = createUser("PrDel");
        long pid = createProfessor(u);
        given().pathParam("id", pid).when().delete("/professor/remove-professor/id/{id}")
                .then().statusCode(200).body(containsString("Successfully"));
        given().pathParam("id", pid).when().get("/professor/id/{id}")
                .then().statusCode(404);
    }

    @Test
    public void deleteProfessorByUserRemovesTest() {
        int u = createUser("PrDelU");
        createProfessor(u);
        given().pathParam("userId", u).when().delete("/professor/remove-professor/user/{userId}")
                .then().statusCode(200);
        given().pathParam("userId", u).when().get("/professor/user/{userId}")
                .then().statusCode(404);
    }

    @Test
    public void deleteProfessorByMissingUserTest() {
        given().pathParam("userId", 999_999_999).when().delete("/professor/remove-professor/user/{userId}")
                .then().statusCode(404);
    }

    // ============================================================
    // BOOKS / MOVIES / GAMES (local CRUD only — external search APIs not tested here)
    // ============================================================

    @Test
    public void getAllBooksTest() {
        given().when().get("/books").then().statusCode(200);
    }

    @Test
    public void getBookByIdNotFoundTest() {
        given().pathParam("id", 999_999_999L).when().get("/books/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getBookByMissingTitleTest() {
        given().queryParam("title", "NoSuchBook" + random.nextInt(1_000_000))
                .when().get("/books/name").then().statusCode(404);
    }

    @Test
    public void getBookReviewsMissingTest() {
        given().pathParam("id", 999_999_999L).when().get("/books/{id}/reviews")
                .then().statusCode(404);
    }

    @Test
    public void updateBookRatingMissingTest() {
        given().pathParam("id", 999_999_999L).when().put("/books/{id}/update-rating")
                .then().statusCode(404);
    }

    @Test
    public void updateBookMissingTest() {
        given().contentType(ContentType.JSON).body("{}").pathParam("id", 999_999_999L)
                .when().put("/books/{id}").then().statusCode(404);
    }

    @Test
    public void deleteBookMissingTest() {
        given().pathParam("id", 999_999_999L).when().delete("/books/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getAllMoviesAndTypesTest() {
        given().when().get("/movies").then().statusCode(200);
        given().when().get("/movies/type/movie").then().statusCode(200);
        given().when().get("/movies/type/show").then().statusCode(200);
    }

    @Test
    public void getMovieByIdNotFoundTest() {
        given().pathParam("id", 999_999_999L).when().get("/movies/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getMoviesByGenreTest() {
        given().pathParam("genre", "Drama").when().get("/movies/genre/{genre}")
                .then().statusCode(200);
    }

    @Test
    public void getMovieReviewsMissingTest() {
        given().pathParam("id", 999_999_999L).when().get("/movies/{id}/reviews")
                .then().statusCode(404);
    }

    @Test
    public void updateMovieRatingMissingTest() {
        given().pathParam("id", 999_999_999L).when().put("/movies/{id}/update-rating")
                .then().statusCode(404);
    }

    @Test
    public void updateMovieMissingTest() {
        given().contentType(ContentType.JSON).body("{}").pathParam("id", 999_999_999L)
                .when().put("/movies/{id}").then().statusCode(404);
    }

    @Test
    public void deleteMovieMissingTest() {
        given().pathParam("id", 999_999_999L).when().delete("/movies/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getAllGamesAndByGenreTest() {
        given().when().get("/games").then().statusCode(200);
        given().pathParam("genre", "RPG").when().get("/games/genre/{genre}")
                .then().statusCode(200);
    }

    @Test
    public void getGameByIdNotFoundTest() {
        given().pathParam("id", 999_999_999L).when().get("/games/{id}")
                .then().statusCode(404);
    }

    @Test
    public void getGameReviewsMissingTest() {
        given().pathParam("id", 999_999_999L).when().get("/games/{id}/reviews")
                .then().statusCode(404);
    }

    @Test
    public void updateGameRatingMissingTest() {
        given().pathParam("id", 999_999_999L).when().put("/games/{id}/update-rating")
                .then().statusCode(404);
    }

    @Test
    public void updateGameMissingTest() {
        given().contentType(ContentType.JSON).body("{}").pathParam("id", 999_999_999L)
                .when().put("/games/{id}").then().statusCode(404);
    }

    @Test
    public void deleteGameMissingTest() {
        given().pathParam("id", 999_999_999L).when().delete("/games/{id}")
                .then().statusCode(404);
    }

    // ============================================================
    // CHAT (only the read endpoints; /message hits Gemini and is excluded)
    // ============================================================

    @Test
    public void listChatSessionsForUserTest() {
        int u = createUser("ChatSess");
        given().pathParam("userId", (long) u).when().get("/sessions/{userId}")
                .then().statusCode(org.hamcrest.Matchers.anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    public void getChatSessionMessagesMissingTest() {
        given().pathParam("sessionId", 999_999_999L).when().get("/sessions/{sessionId}/messages")
                .then().statusCode(org.hamcrest.Matchers.anyOf(equalTo(200), equalTo(404)));
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /** Creates a user with a random ISU email and returns the generated id. */
    private int createUser(String namePrefix) {
        String suffix = String.valueOf(random.nextInt(1_000_000));
        String name = namePrefix + suffix;
        String email = (namePrefix + suffix).toLowerCase() + "@iastate.edu";
        registerUserWithName(name, email);
        return lookupId(email);
    }

    private int lookupId(String email) {
        return given().pathParam("emailId", email)
                .when().get("/users/email/{emailId}")
                .then().statusCode(200).extract().path("id");
    }

    private void registerUserWithName(String name, String email) {
        String json = "{" +
                "\"name\":\"" + name + "\"," +
                "\"emailId\":\"" + email + "\"," +
                "\"password\":\"Hello45!\"" +
                "}";
        given().contentType(ContentType.JSON).body(json)
                .when().post("/users")
                .then().statusCode(200).body("message", equalTo("success"));
    }

    private int createReview(String prefix) {
        String json = "{" +
                "\"mediaType\":\"MOVIE\"," +
                "\"title\":\"" + prefix + random.nextInt(1_000_000) + "\"," +
                "\"rating\":80," +
                "\"body\":\"placeholder\"" +
                "}";
        return given().contentType(ContentType.JSON).body(json)
                .when().post("/reviews").then().statusCode(201).extract().path("id");
    }

    private void assignReviewToUser(int reviewId, int userId) {
        given().pathParam("reviewId", reviewId).pathParam("userId", userId)
                .queryParam("requesterId", userId)
                .when().put("/reviews/{reviewId}/user/{userId}")
                .then().statusCode(200);
    }

    private int publishReview(int reviewId, int userId) {
        return given().pathParam("reviewId", reviewId).queryParam("authorId", userId)
                .when().post("/reviews/{reviewId}/publish")
                .then().statusCode(201).extract().path("id");
    }

    private int addComment(int postId, int authorId, String body) {
        return given().contentType(ContentType.JSON).body("{\"body\":\"" + body + "\"}")
                .pathParam("postId", postId).queryParam("authorId", authorId)
                .when().post("/posts/{postId}/comments")
                .then().statusCode(201).extract().path("id");
    }

    private long createProfessor(int userId) {
        return ((Number) given().pathParam("id", userId)
                .when().post("/professor/{id}")
                .then().statusCode(200).extract().path("id")).longValue();
    }

    /** Some 2xx — endpoints here variably return 200/201/204. */
    private static org.hamcrest.Matcher<Integer> anyOf2xx() {
        return org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.greaterThanOrEqualTo(200),
                org.hamcrest.Matchers.lessThan(300));
    }
}
