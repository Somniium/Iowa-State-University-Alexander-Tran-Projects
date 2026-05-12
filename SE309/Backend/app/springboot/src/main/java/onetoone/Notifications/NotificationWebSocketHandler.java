package onetoone.Notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Handles WebSocket connections for live notifications.
 *
 * Each user connects with their userId in the URL path:
 *   ws://localhost:8080/ws/notifications/{userId}
 *
 * Other parts of the app call NotificationWebSocketHandler.sendToUser(userId, message)
 * to push a notification to a specific connected user.
 *
 * A user can have multiple concurrent sessions (phone + browser, two tabs, etc.) — each
 * one is tracked separately and a notification fans out to all of them.
 */
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    // userId (as String) -> set of active WebSocket sessions for that user.
    // CopyOnWriteArraySet keeps iteration during broadcast() safe even when sessions
    // open/close concurrently.
    private static final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        if (userId == null) {
            log.warn("WebSocket rejected: could not parse userId from path {}",
                    session.getUri() != null ? session.getUri().getPath() : "null");
            try { session.close(CloseStatus.BAD_DATA); } catch (IOException ignored) {}
            return;
        }
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket connected: userId={} sessionId={} (user now has {} active sessions)",
                userId, session.getId(), sessions.get(userId).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = extractUserId(session);
        if (userId == null) return;

        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(session);
            // Drop the entry entirely once the user has no live sessions, so the map doesn't grow unbounded.
            if (userSessions.isEmpty()) {
                sessions.remove(userId, userSessions);
            }
        }
        log.info("WebSocket disconnected: userId={} sessionId={} status={}",
                userId, session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Clients can send a ping; server echoes back to confirm connection
        try {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        } catch (IOException e) {
            log.warn("Error sending pong to sessionId={}: {}", session.getId(), e.getMessage());
        }
    }

    /**
     * Send a JSON notification string to a specific user. Fans out across all
     * of that user's currently-connected devices/tabs.
     * Called by NotificationService whenever an event occurs.
     */
    public void sendToUser(String userId, String jsonMessage) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null || userSessions.isEmpty()) return;

        for (WebSocketSession session : userSessions) {
            if (!session.isOpen()) continue;
            try {
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                log.warn("Failed to send notification to userId={} sessionId={}: {}",
                        userId, session.getId(), e.getMessage());
            }
        }
    }

    /**
     * Broadcast a message to every connected session of every user.
     */
    public void broadcast(String jsonMessage) {
        sessions.forEach((userId, userSessions) -> {
            for (WebSocketSession session : userSessions) {
                if (!session.isOpen()) continue;
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    log.warn("Broadcast failed for userId={} sessionId={}: {}",
                            userId, session.getId(), e.getMessage());
                }
            }
        });
    }

    /** Extracts {userId} from the WebSocket URI path. Returns null if the path is malformed or empty. */
    private String extractUserId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : null;
        if (path == null) return null;
        // path = /ws/notifications/{userId}
        String[] parts = path.split("/");
        if (parts.length == 0) return null;
        String last = parts[parts.length - 1];
        return last.isEmpty() ? null : last;
    }
}
