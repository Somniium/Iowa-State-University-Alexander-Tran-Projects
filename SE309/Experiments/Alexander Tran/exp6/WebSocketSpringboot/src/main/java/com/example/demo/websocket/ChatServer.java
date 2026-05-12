package com.example.demo.websocket;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint("/chat/{username}")
@Component
public class ChatServer {

    private static final Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static final Map<String, Session> usernameSessionMap = new Hashtable<>();

    // roomId -> set of usernames
    private static final Map<String, Set<String>> roomUsersMap = new ConcurrentHashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        logger.info("[onOpen] {}", username);

        if (username == null || username.isBlank()) {
            session.close();
            return;
        }

        if (usernameSessionMap.containsKey(username)) {
            sendJsonToSession(session, systemMessage("Username already exists"));
            session.close();
            return;
        }

        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        sendJsonToUser(username, systemMessage("Welcome to the chat server, " + username));
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onMessage] {}: {}", username, message);

        if (username == null || message == null || message.isBlank()) {
            return;
        }

        JsonNode root;
        try {
            root = mapper.readTree(message);
        } catch (Exception e) {
            sendJsonToUser(username, systemMessage("Invalid JSON message."));
            return;
        }

        String type = text(root, "type");

        if (type == null) {
            sendJsonToUser(username, systemMessage("Message type is required."));
            return;
        }

        switch (type) {
            case "JOIN_ROOM" -> handleJoinRoom(username, root);
            case "CHAT_MESSAGE" -> handleChatMessage(username, root);
            case "USER_TYPING" -> handleTyping(username, root);
            case "DIRECT_MESSAGE" -> handleDirectMessage(username, root);
            case "NOTIFICATION" -> handleNotification(username, root);
            default -> sendJsonToUser(username, systemMessage("Unknown message type: " + type));
        }
    }

    @OnClose
    public void onClose(Session session) {
        String username = sessionUsernameMap.get(session);
        logger.info("[onClose] {}", username);

        if (username == null) {
            return;
        }

        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        // remove from all rooms and notify each room
        roomUsersMap.forEach((roomId, users) -> {
            if (users.remove(username)) {
                broadcastToRoom(roomId, userLeftMessage(username, roomId));
            }
        });

        // clean empty rooms
        roomUsersMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.info("[onError] {}: {}", username, throwable.getMessage());
    }

    // handlers

    private void handleJoinRoom(String username, JsonNode root) {
        String roomId = text(root, "roomId");

        if (roomId == null || roomId.isBlank()) {
            sendJsonToUser(username, systemMessage("roomId is required for JOIN_ROOM"));
            return;
        }

        roomUsersMap.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomUsersMap.get(roomId).add(username);

        broadcastToRoom(roomId, userJoinedMessage(username, roomId));
    }

    private void handleChatMessage(String username, JsonNode root) {
        String roomId = text(root, "roomId");
        String content = text(root, "content");

        if (roomId == null || roomId.isBlank()) {
            sendJsonToUser(username, systemMessage("roomId is required for CHAT_MESSAGE"));
            return;
        }

        if (content == null || content.isBlank()) {
            return;
        }

        if (!isUserInRoom(username, roomId)) {
            sendJsonToUser(username, systemMessage("Join the room before sending messages."));
            return;
        }

        broadcastToRoom(roomId, chatMessage(username, roomId, content));
    }

    private void handleTyping(String username, JsonNode root) {
        String roomId = text(root, "roomId");

        if (roomId == null || roomId.isBlank()) {
            return;
        }

        if (!isUserInRoom(username, roomId)) {
            return;
        }

        String status = text(root, "status");
        if (status == null) status = "START";

        broadcastToRoomExceptSender(roomId, username, typingMessage(username, roomId, status));
    }

    private void handleDirectMessage(String username, JsonNode root) {
        String targetUsername = text(root, "targetUsername");
        String content = text(root, "content");

        if (targetUsername == null || targetUsername.isBlank()) {
            sendJsonToUser(username, systemMessage("targetUsername is required for DIRECT_MESSAGE"));
            return;
        }

        if (content == null || content.isBlank()) {
            return;
        }

        if (!usernameSessionMap.containsKey(targetUsername)) {
            sendJsonToUser(username, systemMessage("User not connected: " + targetUsername));
            return;
        }

        String dmJson = directMessage(username, content);
        sendJsonToUser(targetUsername, dmJson);
        sendJsonToUser(username, dmJson);
    }

    private void handleNotification(String username, JsonNode root) {
        String content = text(root, "content");

        if (content == null || content.isBlank()) {
            return;
        }

        String json = notificationMessage(content);
        usernameSessionMap.keySet().forEach(thisUser -> sendJsonToUser(thisUser, json));
    }

    // helpers

    private boolean isUserInRoom(String username, String roomId) {
        return roomUsersMap.containsKey(roomId) && roomUsersMap.get(roomId).contains(username);
    }

    private void broadcastToRoom(String roomId, String json) {
        Set<String> users = roomUsersMap.get(roomId);
        if (users == null) return;

        for (String user : users) {
            sendJsonToUser(user, json);
        }
    }

    private void broadcastToRoomExceptSender(String roomId, String sender, String json) {
        Set<String> users = roomUsersMap.get(roomId);
        if (users == null) return;

        for (String user : users) {
            if (!user.equals(sender)) {
                sendJsonToUser(user, json);
            }
        }
    }

    private void sendJsonToUser(String username, String json) {
        try {
            Session s = usernameSessionMap.get(username);
            if (s != null && s.isOpen()) {
                s.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            logger.info("[sendJsonToUser Exception] {}", e.getMessage());
        }
    }

    private void sendJsonToSession(Session session, String json) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            logger.info("[sendJsonToSession Exception] {}", e.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    // json builders

    private String systemMessage(String content) {
        return """
            {
              "type":"SYSTEM",
              "content":%s
            }
            """.formatted(toJsonString(content)).replaceAll("\\s+", " ").trim();
    }

    private String userJoinedMessage(String sender, String roomId) {
        return """
            {
              "type":"USER_JOINED",
              "sender":%s,
              "roomId":%s
            }
            """.formatted(toJsonString(sender), toJsonString(roomId)).replaceAll("\\s+", " ").trim();
    }

    private String userLeftMessage(String sender, String roomId) {
        return """
            {
              "type":"USER_LEFT",
              "sender":%s,
              "roomId":%s
            }
            """.formatted(toJsonString(sender), toJsonString(roomId)).replaceAll("\\s+", " ").trim();
    }

    private String chatMessage(String sender, String roomId, String content) {
        return """
            {
              "type":"CHAT_MESSAGE",
              "sender":%s,
              "roomId":%s,
              "content":%s
            }
            """.formatted(toJsonString(sender), toJsonString(roomId), toJsonString(content))
                .replaceAll("\\s+", " ").trim();
    }

    private String typingMessage(String sender, String roomId, String status) {
        return """
        {
          "type":"USER_TYPING",
          "sender":%s,
          "roomId":%s,
          "status":%s
        }
        """.formatted(
                toJsonString(sender),
                toJsonString(roomId),
                toJsonString(status)
        ).replaceAll("\\s+", " ").trim();
    }

    private String directMessage(String sender, String content) {
        return """
            {
              "type":"DIRECT_MESSAGE",
              "sender":%s,
              "content":%s
            }
            """.formatted(toJsonString(sender), toJsonString(content)).replaceAll("\\s+", " ").trim();
    }

    private String notificationMessage(String content) {
        return """
            {
              "type":"NOTIFICATION",
              "content":%s
            }
            """.formatted(toJsonString(content)).replaceAll("\\s+", " ").trim();
    }

    private String toJsonString(String value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            return "\"\"";
        }
    }
}