package onetoone.Followers;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Users.User;

@Getter
@Setter
@Entity
public class Follower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following_id")
    private User following;

    boolean isBlocked;

    public Follower(User follower, User following) {
        this.follower = follower;
        this.following = following;
        this.isBlocked = false;
    }

    public Follower() {}
}
