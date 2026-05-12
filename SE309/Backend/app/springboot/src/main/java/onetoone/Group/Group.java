package onetoone.Group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import onetoone.Messages.Message;
import onetoone.Professors.Professor;
import onetoone.Users.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="groups")
@Getter
@Setter
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_id")
    private Long groupId;

    private String name;

    private String className;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToMany(mappedBy = "groups")
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();


    public Group(String name ,String className) {
        this.name = name;
        this.className = className;
    }

    public Group() {}

    public void addMember(User user) {
        if(!this.members.contains(user)) {
            members.add(user);
            user.addGroup(this);
        }
    }

    public void removeMember(User user) {
        members.remove(user);
        user.removeGroup(this);
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.addGroup(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.removeGroup(this);
    }
}
