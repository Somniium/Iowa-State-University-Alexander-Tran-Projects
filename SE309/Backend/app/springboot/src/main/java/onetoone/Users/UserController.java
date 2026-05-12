package onetoone.Users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import onetoone.Admins.AdminRepository;
import onetoone.Comments.CommentRepository;
import onetoone.Followers.FollowerRepository;
import onetoone.Friends.FriendRepository;
import onetoone.Group.Group;
import onetoone.Likes.LikeRepository;
import onetoone.Messages.MessageRepository;
import onetoone.Notifications.NotificationRepository;
import onetoone.Posts.Post;
import onetoone.Posts.PostRepository;
import onetoone.Professors.Professor;
import onetoone.Professors.ProfessorRepository;
import onetoone.Reviews.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

@RestController
@Tag(name = "Users", description = "Manage user accounts and other tables tied to them.")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FollowerRepository followerRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    MessageRepository messageRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * A real bcrypt hash used only to spend ~the same amount of CPU as a real lookup
     * when /users/login receives an unknown email. Without this, attackers can probe
     * which emails exist by measuring response time. (This is the bcrypt of "x".)
     */
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(
            summary = "Returns a list of all users",
            description = "Returns a list of all users in DB as JSON objects"
    )
    @GetMapping(path = "/users")
    List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @Operation(
            summary = "Returns user by ID",
            description = "Returns a user as a Java object using ID of user in DB"
    )
    @GetMapping(path = "/users/id/{id}")
    ResponseEntity<?> getUserById(
            @Parameter(description = "ID of user in DB", example = "1")
            @PathVariable int id) {
        User user = userRepository.findByUserId(id);

        if(user == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns user by name",
            description = "Returns user as Java object using name of user in DB"
    )
    @GetMapping(path = "/users/name/{name}")
    ResponseEntity<?> getUserByName(
            @Parameter(description = "Name of user in DB", example = "John")
            @PathVariable String name){
       User user = userRepository.findByName(name);

        if(user == null) {
            return new ResponseEntity<>("User not found under name", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(
            description = "Returns user by email",
            summary = "Returns user as a Java object using name of user in DB"
    )
    @GetMapping(path = "/users/email/{email}")
    ResponseEntity<?> getUserByEmail(
            @Parameter(description = "Email attached to user", example = "john@iastate.edu")
            @PathVariable String email){
        User user = userRepository.findByEmailId(email);

        if(user == null) {
            return new ResponseEntity<>("User not found under email", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(
            summary = "Return a user's groups",
            description = "Returns all of the groups that a user owns.",
            parameters = {
                    @Parameter(name = "id", description = "ID of user in DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Groups returned successfully"),
                    @ApiResponse(responseCode = "404", description = "user not found under ID")
            }
    )
    @GetMapping(path="/get-user-groups/{id}")
    ResponseEntity<?> getUserGroups(@PathVariable int id) {
        User user = userRepository.findByUserId(id);

        if(user == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user.getGroups(), HttpStatus.OK);
    }

    @Operation(
            summary = "Creates a user",
            description = "Adds a user to the DB using a JSON object"
    )
    @PostMapping(path = "/users")
    String createUser(@Valid
                      @Parameter(description = "JSON object of user")
                      @RequestBody User user) {
        String e = user.getEmailId();
        String name = user.getName();

        // Validate email presence + ISU domain BEFORE any DB lookup.
        if (e == null || !e.contains("iastate.edu")) {
            return "{\"message\":\"Error: ISU email required to signup for CyVal.\"}";
        }

        // Checks if username is in use.
        if (userRepository.findByName(name) != null) {
            return "{\"message\":\"Error: User with given name already exists.\"}";
        }

        // Checks if email is in use.
        if (userRepository.findByEmailId(e) != null) {
            return "{\"message\":\"Error: User with given email already exists.\"}";
        }

        // Hash password before persisting — never store plaintext.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return success;
    }

    @Operation(
            summary = "Updates user",
            description = "Updates user in DB using an ID and a JSON object of the user"
    )
    @PutMapping("/users/{id}")
    ResponseEntity<?> updateUser(
            @Parameter(description = "ID of user")
            @PathVariable int id,
            @Parameter(description = "JSON Object of user")
            @RequestBody User request){
        User existing = userRepository.findByUserId(id);

        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User id does not exist");
        }

        // Field-by-field patching — never overwrite the whole entity from raw request,
        // which would let callers null out fields or change another user's record.
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getEmailId() != null) {
            existing.setEmailId(request.getEmailId());
        }
        if (request.getPassword() != null) {
            // Hash any new password before persisting.
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @Operation(
            summary = "Deletes user by ID",
            description = "Deletes user object from DB using ID for user in DB"
    )
    @Transactional
    @DeleteMapping(path = "/users/id/{id}")
    ResponseEntity<?> deleteUser(
            @Parameter(description = "ID of user")
            @PathVariable int id) {

        User user = userRepository.findByUserId(id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"User not found under ID\"}");
        }

        deleteUserAndReferences(user);

        return ResponseEntity.ok(success);
    }

    @Operation(
            summary = "Deletes user by email",
            description = "Deletes user object from DB using email for user in DB"
    )
    @DeleteMapping(path = "/users/email/{emailId}")
    @Transactional
    ResponseEntity<?> deleteUserByEmail(
            @Parameter(description = "Email of user")
            @PathVariable String emailId) {
        User user = userRepository.findByEmailId(emailId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"User not found under email\"}");
        }

        deleteUserAndReferences(user);
        return ResponseEntity.ok(success);
    }

    private void deleteUserAndReferences(User user) {
        int userId = user.getId();

        for (Group group : new HashSet<>(user.getGroups())) {
            group.removeMember(user);
        }

        messageRepository.clearUserByUserId(userId);
        notificationRepository.deleteByRecipientId(userId);
        likeRepository.deleteByUserId(userId);
        commentRepository.deleteByAuthorId(userId);
        followerRepository.deleteByUserId(userId);
        friendRepository.deleteByUserId(userId);

        List<Post> posts = new ArrayList<>(postRepository.findByAuthorOrReviewUserId(userId));
        postRepository.deleteAll(posts);

        // Posts wrap Reviews — once the user's posts are gone, drop their reviews too
        // so feeds don't try to render reviews pointing at a deleted author.
        reviewRepository.deleteByUser_Id(userId);

        Professor professor = professorRepository.findByUser(user);
        if (professor != null) {
            for (Group group : new ArrayList<>(professor.getGroups())) {
                group.setProfessor(null);
            }
            professor.getGroups().clear();
            professorRepository.delete(professor);
        }

        adminRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    // LOGIN
    static class LoginRequest {
        public String emailId;
        public String password;
    }

    static class LoginResponse {
        public int id;
        public String name;
        public String emailId;
        public boolean active;
        public LoginResponse(User u) {
            this.id = u.getId();
            this.name = u.getName();
            this.emailId = u.getEmailId();
            this.active = u.isActive();
        }
    }

    @Operation(
            summary = "Used for user login",
            description = "Uses a login request object to retrieve user data and login. " +
                    "Returns a generic 401 for any auth failure so callers can't enumerate which emails are registered."
    )
    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request == null || request.emailId == null || request.password == null) {
            return ResponseEntity.badRequest().body("{\"message\":\"Missing email or password\"}");
        }

        User user = userRepository.findByEmailId(request.emailId);

        // Single generic response for "no such user", "account inactive", and "wrong password".
        // This stops attackers from probing /users/login to learn which ISU emails have accounts.
        // We still run bcrypt on a throwaway hash when the user is missing so response time
        // doesn't trivially leak existence either.
        boolean credentialsValid =
                user != null
                        && user.isActive()
                        && passwordEncoder.matches(request.password, user.getPassword());

        if (user == null) {
            // Constant-time-ish dummy compare so the not-found path takes ~the same time as the matched path.
            passwordEncoder.matches(request.password, DUMMY_BCRYPT_HASH);
        }

        if (!credentialsValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\":\"Invalid email or password\"}");
        }

        // return DTO, not entity (prevents recursion/lazy-load 500)
        return ResponseEntity.ok(new LoginResponse(user));
    }
}
