package onetoone.Followers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FollowerController {

    @Autowired
    FollowerRepository followerRepository;

    @Autowired
    UserRepository userRepository;

    @Operation(
            summary = "Return all followers",
            description = "Return all follower objects in DB",
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully returned all followers.")
            }
    )
    @GetMapping(path = "/followers")
    List<Follower> getAllFollowers() {
        return followerRepository.findAll();
    }

    @Operation(
            summary = "Get follower count",
            description = "Returns the follower count of a user using the ID of the user in the DB.",
            parameters = {
                @Parameter(name = "followerUserId", description = "ID of the user in the DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned follower count."),
                    @ApiResponse(responseCode = "404", description = "User not found.")
            }
    )
    @GetMapping(path = "/follower/get-follower-count/{followerUserId}")
    ResponseEntity<?> getFollowerCount(@PathVariable int followerUserId) {
        User currUser = userRepository.findByUserId(followerUserId);

        if(currUser == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(followerRepository.countByFollowingUserId(followerUserId), HttpStatus.OK);
    }

    @Operation(
            summary = "Get following count",
            description = "Returns the following count of a user using the ID of the user in the DB.",
            parameters = {
                    @Parameter(name = "followingUserId", description = "ID of the user in the DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned following count."),
                    @ApiResponse(responseCode = "404", description = "User not found.")
            }
    )
    @GetMapping(path = "/follower/get-following-count/{followingUserId}")
    ResponseEntity<?> getFollowingCount(@PathVariable int followingUserId) {
        User currUser = userRepository.findByUserId(followingUserId);

        if(currUser == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(followerRepository.countByFollowerUserId(followingUserId), HttpStatus.OK);
    }

    @Operation(
            summary = "Returns following",
            description = "Returns a list of all users given user is following.",
            parameters = {
                @Parameter(name = "userId", description = "ID of user in DB.")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully returned list of following."),
                @ApiResponse(responseCode = "404", description = "User not found under ID.")
            }
    )
    @GetMapping(path = "/follower/get-following/{userId}")
    ResponseEntity<?> getFollowing(@PathVariable int userId) {
        User currUser = userRepository.findByUserId(userId);

        if(currUser == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        List<User> following = followerRepository.findFollowingByUserId(userId);

        return new ResponseEntity<>(following, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns followers",
            description = "Returns a list of all followers of user.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of user in DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned list of followers."),
                    @ApiResponse(responseCode = "404", description = "User not found under ID.")
            }
    )
    @GetMapping(path = "/follower/get-followers/{userId}")
    ResponseEntity<?> getFollowers(@PathVariable int userId) {
        User currUser = userRepository.findByUserId(userId);

        if(currUser == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        List<User> following = followerRepository.findFollowersByUserId(userId);

        return new ResponseEntity<>(following, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns block status",
            description = "Returns whether or not a follower is blocked.",
            parameters = {
                    @Parameter(name = "followerId", description = "ID of follower relationship in DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned blocked state."),
                    @ApiResponse(responseCode = "404", description = "One or both users not found.")
            }
    )
    @GetMapping(path = "/follower/block-status/{followerId}")
    ResponseEntity<?> blockedStatus(@PathVariable long followerId) {
        Follower exists = followerRepository.findById(followerId);

        if(exists == null) {
            return new ResponseEntity<>("Follower not found.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(exists.isBlocked, HttpStatus.OK);
    }

    @Operation(
            summary = "Follow a user",
            description = "Follow a user using the ID of current user and user to follow in DB.",
            parameters = {
                @Parameter(name = "userId", description = "ID of current user in DB."),
                @Parameter(name = "followId", description = "ID of user to follow in DB.")
            },
            responses = {
                @ApiResponse(responseCode = "201", description = "Successfully followed user."),
                @ApiResponse(responseCode = "404", description = "One or both users not found.")
            }
    )
    @PostMapping(path = "/follower/follow-user/currUser/{userId}/followUser/{followId}")
    ResponseEntity<?> followUser(@PathVariable int userId, @PathVariable int followId) {
        User currUser = userRepository.findByUserId(userId);
        User followUser = userRepository.findByUserId(followId);

        if (currUser == null || followUser == null) {
            return new ResponseEntity<>("One or both users not found", HttpStatus.NOT_FOUND);
        }

        // Prevent duplicate follow relationships.
        if (followerRepository.findByFollowerUserIdAndFollowingUserId(userId, followId) != null) {
            return new ResponseEntity<>("Already following this user", HttpStatus.CONFLICT);
        }

        Follower follow = new Follower(currUser, followUser);
        followerRepository.save(follow);

        return new ResponseEntity<>(follow, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Block a follower",
            description = "Block a follower using DB ID of follower relationship",
            parameters = {
                @Parameter(name = "followerId", description = "ID of follower relationship in DB")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully blocked follower"),
                @ApiResponse(responseCode = "404", description = "Follower relationship not found")
            }
    )
    @PutMapping(path = "/follower/block-follower/{followerId}")
    ResponseEntity<?> blockUser(@PathVariable long followerId) {
        Follower exists = followerRepository.findById(followerId);

        if(exists == null) {
            return new ResponseEntity<>("Follower not found.", HttpStatus.NOT_FOUND);
        }

        exists.setBlocked(true);
        followerRepository.save(exists);

        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Unblock a follower",
            description = "Unblock a follower using DB ID of follower relationship",
            parameters = {
                    @Parameter(name = "followerId", description = "ID of follower relationship in DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully unblocked follower"),
                    @ApiResponse(responseCode = "404", description = "Follower relationship not found")
            }
    )
    @PutMapping(path = "/follower/unblock-follower/{followerId}")
    ResponseEntity<?> unblockUser(@PathVariable long followerId) {
        Follower exists = followerRepository.findById(followerId);

        if(exists == null) {
            return new ResponseEntity<>("Follower not found.", HttpStatus.NOT_FOUND);
        }

        exists.setBlocked(false);
        followerRepository.save(exists);

        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Remove a follower",
            description = "Removes a follower using the DB ID of the follower.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of current user in DB"),
                    @Parameter(name = "followId", description = "ID of followed user in DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully removed follower"),
                    @ApiResponse(responseCode = "404", description = "Follower relationship not found")
            }
    )
    @DeleteMapping(path = "/follower/remove-follower/currUser/{userId}/followUser/{followId}")
    ResponseEntity<?> removeFollower(@PathVariable int userId, @PathVariable int followId) {
        Follower exists = followerRepository.findByFollowerUserIdAndFollowingUserId(userId, followId);

        if(exists == null) {
            return new ResponseEntity<>("Follower not found.", HttpStatus.NOT_FOUND);
        }

        followerRepository.deleteByUsers(userId, followId);
        return new ResponseEntity<>("Successfully removed follower.", HttpStatus.OK);
    }
}
