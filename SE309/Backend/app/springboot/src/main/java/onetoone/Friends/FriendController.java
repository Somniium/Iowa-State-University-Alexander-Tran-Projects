package onetoone.Friends;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Friends", description = "Send and modify friend requests and check friendships.")
public class FriendController {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    UserRepository userRepository;

    @Operation(
            summary = "Return all friends",
            description = "Returns a list of all friends in DB",
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully returned all friends")
            }
    )
    @GetMapping(path = "/friends")
    List<Friend> getAllFriends() {
        return friendRepository.findAll();
    }

    @Operation(
            summary = "Get outgoing friend requests",
            description = "Returns all friend objects where the given user is the sender.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user who sent the requests")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned sent requests"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping(path = "/friends/sent-by/{userId}")
    ResponseEntity<?> getFriendRequestsSentByUser(@PathVariable int userId) {
        if (userRepository.findByUserId(userId) == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(friendRepository.findByUserUserId(userId), HttpStatus.OK);
    }

    @Operation(
            summary = "Get incoming friend requests",
            description = "Returns all friend objects where the given user is the receiver.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user who received the requests")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned received requests"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping(path = "/friends/received-by/{userId}")
    ResponseEntity<?> getFriendRequestsReceivedByUser(@PathVariable int userId) {
        if (userRepository.findByUserId(userId) == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(friendRepository.findByFriendUserId(userId), HttpStatus.OK);
    }

    @Operation(
            summary = "Check request status",
            description = "Check the status of a request between 2 users.",
            parameters = {
                    @Parameter(name = "userId", description = "User sending the request"),
                    @Parameter(name = "friendId", description = "User receiving the request")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned status"),
                    @ApiResponse(responseCode = "404", description = "Request not found")
            }
    )
    @GetMapping(path = "/friend/request-status/currUser/{userId}/friend/{friendId}")
    ResponseEntity<?> getFriendshipStatus(@PathVariable int userId, @PathVariable int friendId) {
        Friend request = friendRepository.findFriendBetweenUsers(userId, friendId);

        if(request == null) {
            return new ResponseEntity<>("Request not found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(request.getFriendStatus(), HttpStatus.OK);
    }

    @Operation(
            summary = "Create a friend request",
            description = "Creates a friend request using the IDs of the sending and receiving users in the DB.",
            parameters = {
                    @Parameter(name = "userId", description = "User sending the request"),
                    @Parameter(name = "friendId", description = "User receiving the request")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created request"),
                    @ApiResponse(responseCode = "404", description = "One or both users not found")
            }
    )
    @PostMapping(path = "/friend/send-request/currUser/{userId}/friend/{friendId}")
    ResponseEntity<?> sendFriendRequest(@PathVariable int userId, @PathVariable int friendId) {
        User currUser = userRepository.findByUserId(userId);
        User friend = userRepository.findByUserId(friendId);

        if (currUser == null || friend == null) {
            return new ResponseEntity<>("User or friend not found", HttpStatus.NOT_FOUND);
        }

        // Prevent duplicate requests / re-friending an existing relationship.
        if (friendRepository.findFriendBetweenUsers(userId, friendId) != null) {
            return new ResponseEntity<>("Friend request or relationship already exists", HttpStatus.CONFLICT);
        }

        Friend request = new Friend(currUser, friend, "PENDING");
        friendRepository.save(request);

        return new ResponseEntity<>(request, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Accept a friend request",
            description = "Changes the status of a friend request to accepted using ID of request in DB",
            parameters = {
                @Parameter(name = "requestId", description = "ID of request in DB.")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully accepted request."),
                @ApiResponse(responseCode = "404", description = "Request not found in DB.")
            }
    )
    @PutMapping(path = "/friend/accept-request/{requestId}")
    ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId) {
        Friend request = friendRepository.findById(requestId).orElse(null);

        if(request == null) {
            return new ResponseEntity<>("Request not found", HttpStatus.NOT_FOUND);
        }

        request.setFriendStatus("ACCEPTED");
        friendRepository.save(request);

        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    @Operation(
            summary = "Decline a friend request",
            description = "Changes the status of a friend request to declined using ID of request in DB",
            parameters = {
                    @Parameter(name = "requestId", description = "ID of request in DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully declined request."),
                    @ApiResponse(responseCode = "404", description = "Request not found in DB.")
            }
    )
    @PutMapping(path = "/friend/decline-request/{requestId}")
    ResponseEntity<?> declineFriendRequest(@PathVariable Long requestId) {
        Friend request = friendRepository.findById(requestId).orElse(null);

        if(request == null) {
            return new ResponseEntity<>("Request not found", HttpStatus.NOT_FOUND);
        }

        request.setFriendStatus("DECLINED");
        friendRepository.save(request);

        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    @Operation(
            summary = "Remove a friend or cancel request.",
            description = "Remove a friend or cancel request using its ID in the DB",
            parameters = {
                    @Parameter(name = "requestId", description = "ID of request in DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully removed friend or cancelled request."),
                    @ApiResponse(responseCode = "404", description = "Request not found in DB.")
            }
    )
    @DeleteMapping(path = "/friend/remove-friend-or-cancel-request/{requestId}")
    ResponseEntity<?> removeFriend(@PathVariable Long requestId) {
        Friend exists = friendRepository.findById(requestId).orElse(null);

        if(exists == null) {
            return new ResponseEntity<>("Request not found", HttpStatus.NOT_FOUND);
        }

        friendRepository.deleteById(requestId);

        return new ResponseEntity<>("Successfully removed friend or cancelled request", HttpStatus.OK);
    }
}
