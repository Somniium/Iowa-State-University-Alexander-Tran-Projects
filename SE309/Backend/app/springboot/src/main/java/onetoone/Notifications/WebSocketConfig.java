package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration - registers the notification endpoint.
 *
 * Connect via: ws://localhost:8080/ws/notifications/{userId}
 *
 * Allowed origins are configurable via the `cyval.allowed-origins` property
 * (comma-separated list). Defaults to localhost dev origins. Never use "*"
 * here — any site could otherwise open a socket and read another user's
 * notifications by guessing user IDs.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${cyval.allowed-origins:http://localhost:8080,http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public NotificationWebSocketHandler notificationWebSocketHandler() {
        return new NotificationWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler(), "/ws/notifications/{userId}")
                .setAllowedOrigins(allowedOrigins);
    }
}
