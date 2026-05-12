package onetoone.Admins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Users.User;

// Comment to test pipeline

@Getter
@Setter
@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isMaster = false;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Admin(User user) {
        this.user = user;
    }

    public Admin() {}

    public void addUser(User user) {
        this.user = user;
    }

    public void removeUser() {
        this.user = null;
    }
}
