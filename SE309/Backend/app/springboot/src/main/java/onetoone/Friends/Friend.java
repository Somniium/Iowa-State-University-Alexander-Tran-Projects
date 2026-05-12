package onetoone.Friends;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Users.User;

@Getter
@Setter
@Entity
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    // DECLINED, PENDING, ACCEPTED
    private String friendStatus;

    public Friend(User user, User friend, String friendStatus) {
        this.user = user;
        this.friend = friend;
        this.friendStatus = friendStatus;
    }

    public Friend() {}


}
