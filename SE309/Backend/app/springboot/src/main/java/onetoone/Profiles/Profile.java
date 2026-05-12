package onetoone.Profiles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Users.User;

@Getter
@Setter
@Entity
public class Profile {

    @Id
    @Column(name = "user_id")
    private int profileId;

    private String bio;

    private String major;

    private String hobbies;

    private String gradDate;

    private String linkedInURL;

    @OneToOne
    @MapsId
    @JoinColumn(name="user_id")
    @JsonIgnore
    private User user;

    public Profile (String bio, String hobbies, String gradDate, String linkedInURL) {
        this.bio = bio;
        this.hobbies = hobbies;
        this.gradDate = gradDate;
        this.linkedInURL = linkedInURL;
    }

    public Profile() {}

    public void addUser(User user) {
        this.user = user;
    }

    public void removeUser() {
        this.user = null;
    }
}
