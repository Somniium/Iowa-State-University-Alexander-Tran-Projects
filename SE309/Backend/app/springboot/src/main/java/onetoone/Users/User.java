package onetoone.Users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import onetoone.Friends.Friend;
import onetoone.Group.Group;
import onetoone.Messages.Message;
import onetoone.Profiles.Profile;
import onetoone.Reviews.Review;

/**
 * 
 * @author Alex Tran and Kamil Halupka
 * 
 */ 

@Entity
@Table(name = "users")
public class User {

    /*
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int userId;
    private String name;
     /*
     * Email annotation is part of the hibernate validator package that helps with validation of email
     * input. In which case, it currently is matching to the regexp that expresses any characters are allowed followed by an '@' except for '|' and '
     * as they are potential SQL injection risk. Flag here is used to discern that input is not case-sensitive.
     */
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE)

    private String emailId;

    @NotBlank(message = "Please enter a password.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!?@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one number, one lowercase letter, one uppercase letter, and one" +
                    "special character (@#$%^&+=)"
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean active = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @ManyToMany
    @JoinTable(
            name = "users_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @JsonIgnore
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Friend> sentFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Friend> receivedFriendRequests = new ArrayList<>();

    public User(String name, String emailId, String password) {
        this.name = name;
        this.emailId = emailId;
        this.password = password;
    }

    public User() {
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setUser(this);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        review.setUser(null);
    }

    public void addGroup(Group group) {
        if (!this.groups.contains(group)) {
            groups.add(group);
        }
    }

    public void removeGroup(Group group) {
        groups.remove(group);
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.addUser(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.removeUser(this);
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public boolean isActive() {
        return active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public List<Message> getMessages() {
        return this.messages;
    }

    @JsonIgnore
    public Set<Group> getGroups() {
        return this.groups;
    }
}

