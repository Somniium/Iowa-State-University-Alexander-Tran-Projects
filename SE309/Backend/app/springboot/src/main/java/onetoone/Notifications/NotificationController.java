package onetoone.Notifications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Notifications", description = "Retrieve and manage user notifications. Live delivery via WebSocket: ws://localhost:8080/ws/notifications/{userId}")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(
            summary = "Get all notifications for a user",
            description = "Returns all notifications (read and unread) newest first.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of notifications",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Notification.class)))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<?> getUserNotifications(
            @Parameter(description = "ID of the user", required = true) @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.ok(notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId));
    }

    @Operation(
            summary = "Get unread notifications for a user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unread notifications",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Notification.class)))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/notifications/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @Parameter(description = "ID of the user", required = true) @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        return ResponseEntity.ok(notificationRepository.findByRecipient_IdAndReadFalseOrderByCreatedAtDesc(userId));
    }

    @Operation(
            summary = "Get unread notification count (badge)",
            description = "Use this to drive a badge counter in the UI.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JSON unread count",
                            content = @Content(schema = @Schema(example = "{\"unread\":3}"))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @GetMapping("/users/{userId}/notifications/count")
    public ResponseEntity<?> getUnreadCount(
            @Parameter(description = "ID of the user", required = true) @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        long count = notificationRepository.countByRecipient_IdAndReadFalse(userId);
        return ResponseEntity.ok("{\"unread\":" + count + "}");
    }

    @Operation(
            summary = "Mark a single notification as read",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated notification",
                            content = @Content(schema = @Schema(implementation = Notification.class))),
                    @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content)
            }
    )
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable int id) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok(n);
    }

    @Operation(
            summary = "Mark ALL notifications as read for a user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All marked as read",
                            content = @Content(schema = @Schema(example = "{\"message\":\"All notifications marked as read\"}")))
            }
    )
    @PutMapping("/users/{userId}/notifications/read-all")
    public ResponseEntity<?> markAllRead(
            @Parameter(description = "ID of the user", required = true) @PathVariable int userId) {
        List<Notification> unread = notificationRepository.findByRecipient_IdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok("{\"message\":\"All notifications marked as read\"}");
    }

    @Operation(
            summary = "Delete a notification",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted",
                            content = @Content(schema = @Schema(example = "{\"message\":\"success\"}"))),
                    @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content)
            }
    )
    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable int id) {
        if (!notificationRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        notificationRepository.deleteById(id);
        return ResponseEntity.ok("{\"message\":\"success\"}");
    }
}
