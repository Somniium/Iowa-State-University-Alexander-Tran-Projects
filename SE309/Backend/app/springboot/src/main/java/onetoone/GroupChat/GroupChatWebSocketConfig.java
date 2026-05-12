package onetoone.GroupChat;

import onetoone.Admins.AdminRepository;
import onetoone.Group.GroupRepository;
import onetoone.Messages.MessageRepository;
import onetoone.Professors.ProfessorRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 *
 * What happens here is that the serverendpoint -- in this case it is
 * the /chat endpoint handler is registered with SPRING
 * so that requests to ws:// will be honored.
 */
@Configuration
public class GroupChatWebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }

    @Autowired
    public void setRepos(UserRepository userRepository, MessageRepository messageRepository, GroupRepository groupRepository, AdminRepository adminRepository, ProfessorRepository professorRepository) {
        ChatServer.setGroupRepository(groupRepository);
        ChatServer.setMessageRepository(messageRepository);
        ChatServer.setUserRepository(userRepository);
        ChatServer.setAdminRepository(adminRepository);
        ChatServer.setProfessorRepository(professorRepository);
    }
}