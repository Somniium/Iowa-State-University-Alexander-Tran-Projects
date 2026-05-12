package onetoone.Notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import onetoone.Users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Central service for creating, persisting, and pushing notifications.
 *
 * Usage (inject NotificationService wherever events happen):
 *
 *   notificationService.notify(recipientUser, "LIKE",
 *       actorName + " liked your review of " + reviewTitle);
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationWebSocketHandler wsHandler;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Persist a notification and push it live over WebSocket if the recipient is connected.
     *
     * @param recipient the user who should receive the notification
     * @param type      short tag: COMMENT | LIKE | REVIEW
     * @param message   human-readable notification text
     */
    public Notification notify(User recipient, String type, String message) {
        Notification n = new Notification(recipient, type, message);
        notificationRepository.save(n);

        try {
            String json = mapper.writeValueAsString(n);
            wsHandler.sendToUser(String.valueOf(recipient.getId()), json);
        } catch (Exception e) {
            System.err.println("Could not push WS notification: " + e.getMessage());
        }

        return n;
    }
}
