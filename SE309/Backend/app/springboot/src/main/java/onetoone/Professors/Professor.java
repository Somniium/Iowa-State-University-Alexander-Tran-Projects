package onetoone.Professors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Group.Group;
import onetoone.Users.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    private List<String> classes = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "professor")
    @JsonIgnore
    private List<Group> groups = new ArrayList<>();

    public Professor(User user) {
        this.user = user;
    }

    public Professor() {}

    public void addClass(String className) {
        this.classes.add(className);
    }

    public void removeClass(String className) {
        this.classes.remove(className);
    }

    public boolean classExists(String className) {
        return classes.contains(className);
    }

    public void addGroup(Group group) {
        groups.add(group);
        group.setProfessor(this);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
        group.setProfessor(null);
    }
}
