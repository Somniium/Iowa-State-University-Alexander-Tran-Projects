package onetoone.GroupChat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import onetoone.Admins.AdminRepository;
import onetoone.Group.Group;
import onetoone.Group.GroupRepository;
import onetoone.Messages.Message;
import onetoone.Messages.MessageRepository;
import onetoone.Professors.ProfessorRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.hibernate.Hibernate;
import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.hibernate.validator.internal.util.logging.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@ServerEndpoint("/chat/group/{groupId}/user/{userId}")
@Component
public class ChatServer {

    private static UserRepository userRepository;
    private static MessageRepository messageRepository;
    private static GroupRepository groupRepository;
    private static ProfessorRepository professorRepository;
    private static AdminRepository adminRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Declarations for repositories.
     */

    public static void setUserRepository(UserRepository repository) { userRepository = repository; }

    public static void setMessageRepository(MessageRepository repository) {
        messageRepository = repository;
    }

    public static void setGroupRepository(GroupRepository repository) {
        groupRepository = repository;
    }

    public static void setAdminRepository(AdminRepository repository) {
        adminRepository = repository;
    }

    public static void setProfessorRepository(ProfessorRepository repository) {
        professorRepository = repository;
    }

    /*
        Maps the session to a group using the groupId. Uses username as a string.
     */
    private static Map<Long, Map<Session, String>> groupSessionMap= new Hashtable <> ();

    private final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    /*
        Defines what happens when a connection is opened to the server socket.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("groupId") Long groupId, @PathParam("userId") int userId) throws IOException {
        User exists = userRepository.findByUserId(userId);
        Group group = groupRepository.findByGroupIdWithDetails(groupId);


        if(exists == null || group == null) {
            session.close();
            return;
        }

        boolean isMember = false;

        for(User m : group.getMembers()) {
            if(m.getId() == exists.getId()) {
                isMember = true;
                break;
            }
        }

        // Guard against a group that has no assigned professor before dereferencing.
        onetoone.Professors.Professor groupProf = group.getProfessor();
        if (groupProf != null && groupProf.getUser() != null && groupProf.getUser().getId() == userId) {
            isMember = true;
        }

        if(!isMember) {
            logger.error("User is not a member of this group.");
            session.close();
            return;
        }

        Hibernate.initialize(exists.getMessages());

        String username = exists.getName();

        logger.info("[onOpen] " + username);

        Map<Session, String> innerMap = groupSessionMap.computeIfAbsent(groupId, k -> new Hashtable<>());

        if (innerMap.containsValue(username)) {
            session.getBasicRemote().sendText("Username already exists");
            session.close();
            return;
        }

        // Maps user to session in hash table.
        innerMap.put(session, username);
        String userType;

        // Logic for determining user type (for flair in front of name)
        if(adminRepository.findByUser(exists) != null) {
            userType = "Admin";
        }
        else if(professorRepository.findByUser(exists) != null) {
            userType = "Professor";
        }
        else {
            userType = "Student";
        }

        // Logic for loading previous messages from group.
        // Saved messages to a set, then use the set to create an ArrayList to prevent duplicate messages.
        Set<Message> uniqueMessages = new LinkedHashSet<>(group.getMessages());
        List<Message> messageHistory = new ArrayList<>(uniqueMessages);

        for (Message m : messageHistory) {
            Hibernate.initialize(m.getUser());

            String usernameHist = m.getUser() != null ? m.getUser().getName() : "Unknown";

            // Use the null-safe usernameHist — not m.getUser().getName() — to avoid NPE.
            session.getBasicRemote().sendText("[" + userType + "] " + usernameHist + ": " + m.getBody());
        }

        broadcast("[" + userType + "] " + username + " has Joined the Chat", groupId);
    }

    /*
        Defines what happens when a message is sent from the client to the server.
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("groupId") Long groupId) throws IOException {
        Map<Session, String> innerMap = groupSessionMap.computeIfAbsent(groupId, k -> new Hashtable<>());
        Group group = groupRepository.findByGroupIdWithDetails(groupId);

        // Saved messages to a set, then use the set to create an ArrayList to prevent duplicate messages.
        Set<Message> uniqueMessages = new LinkedHashSet<>(group.getMessages());
        List<Message> messageHistory = new ArrayList<>(uniqueMessages);

        String username = innerMap.get(session);

        User exists = userRepository.findByName(username);

        if(exists == null) {
            logger.error("User not found.");
            return;
        }

        // Logic for determining user type (for flair in front of name)
        String userType;

        if(adminRepository.findByUser(exists) != null) {
            userType = "Admin";
        }
        else if(professorRepository.findByUser(exists) != null) {
            userType = "Professor";
        }
        else {
            userType = "Student";
        }

        // Builds message object to save it to the DB.
        String timeSent = LocalDateTime.now().format(formatter);
        Message mess = new Message(message, timeSent);

        mess.addUser(exists);
        mess.addGroup(ChatServer.groupRepository.findByGroupId(groupId));

        messageRepository.save(mess);

        logger.info("[onMessage] " + "[" + userType + "]" + ": " + message);


        // Logic for DMs
        if (message.startsWith("@")) {

            // split by space
            String[] split_msg =  message.split("\\s+");

            // Combine the rest of message
            StringBuilder actualMessageBuilder = new StringBuilder();
            for (int i = 1; i < split_msg.length; i++) {
                actualMessageBuilder.append(split_msg[i]).append(" ");
            }
            String destUserName = split_msg[0].substring(1);    //@username and get rid of @
            String actualMessage = actualMessageBuilder.toString();

            Session otherSession = null;

            for(Map.Entry<Session, String> e : innerMap.entrySet()) {
                if(e.getValue().equals(destUserName)) {
                    otherSession = e.getKey();
                }
            }

            if (otherSession != null) {
                sendMessageToPArticularUser(session, "[DM from " + username + "]: " + actualMessage, groupId);
                sendMessageToPArticularUser(otherSession, "[DM from " + username + "]: " + actualMessage, groupId);
            }
            else {
                sendMessageToPArticularUser(session, "Message could not be sent.", groupId);
            }
        }
        else if(message.startsWith("/")) { // Logic for searching for a message
            String[] split_msg_search = message.split("/");
            logger.info(split_msg_search[1]);

            for(Message m : messageHistory) {
                if(m.getBody().contains(split_msg_search[1])) {
                    sendMessageToPArticularUser(session,"From Search: " + "[" + userType + "] " + username + ": " + m.getBody(), groupId);
                }
            }
        }
        else { // Message to whole chat
            broadcast("[" + userType + "] " + username + ": " + message, groupId);
        }
    }

    /*
        Defines what happens when a client disconnects from the server.
     */
    @OnClose
    public void onClose(Session session, @PathParam("groupId") Long groupId) throws IOException {
        Map<Session, String> innerMap = groupSessionMap.get(groupId);

        String username = "Unknown";

        if(innerMap != null) {
            username = innerMap.get(session);
            logger.info("[onClose] " + username);
            innerMap.remove(session);
        }

        broadcast(username + ": " + "Disconnected from the chat.", groupId);
    }

    /*
        Defines what happens when an error is detected.
     */
    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("groupId") Long groupId) {
        Map<Session, String> innerMap = groupSessionMap.get(groupId);

        String username = "Unknown";

        if(innerMap != null && innerMap.containsKey(session)) {
            username = innerMap.get(session);
        }

        logger.info("[onError]" + username + ": " + throwable.getMessage());
    }

    /*
        Helper method used for DMs.
     */
    private void sendMessageToPArticularUser(Session session, String message, @PathParam("groupId") Long groupId) {
        try {
            //usernameSessionMap.get(username).getBasicRemote().sendText(message);
            session.getBasicRemote().sendText(message);

        } catch (IOException e) {
            logger.info("Exception occurred while trying DM: " + e.getMessage());
        }
    }

    /*
        Helper method used to broadcast a message to the whole chat.
     */
    private void broadcast(String message, Long groupId) {
        Map<Session, String> groupSession = groupSessionMap.get(groupId);

        if(groupSession != null) {
                groupSession.forEach((session, s) -> {
                    try {
                        if (session.isOpen()) {
                            session.getBasicRemote().sendText(message);
                        }
                    } catch (IOException e) {
                        logger.info("Exception occurred: " + e.getMessage());
                    }
                });
        }
    }
}