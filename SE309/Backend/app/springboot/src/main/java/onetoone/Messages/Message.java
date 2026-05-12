package onetoone.Messages;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Group.Group;
import onetoone.Users.User;

import java.time.LocalDateTime;

@Entity
@Table(name="messages")
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    private String body;

    @ManyToOne
    @JoinColumn(name="group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private String timeSent;

    public Message(String body, String timeSent) {
        this.body = body;
        this.timeSent = timeSent;
    }

    public Message() {}

    public void addGroup(Group group) {
        this.group = group;
    }

    public void removeGroup(Group group) {
        this.group = null;
    }

    public void addUser(User user) {
        this.user = user;
    }

    public void removeUser(User user) {
        this.user = null;
    }
}
