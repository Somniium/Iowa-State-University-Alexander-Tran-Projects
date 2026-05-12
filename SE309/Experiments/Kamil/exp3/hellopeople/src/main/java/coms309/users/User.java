package coms309.users;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Class that represents a user on the CyVal platform.
 *
 * @author Kamil Halupka
 */
@Getter
@Setter
@NoArgsConstructor // Default constructor
public class User {

    private String username;

    @NotBlank(message = "Please enter something in the email field.")
    @Email(message = "Please enter a valid email.")
    private String email;

    private String followerCount;

    private String numPosts;

    private String permissions;

    public User(String username, String email, String followerCount, String numPosts, String permissions) {
        this.username = username;
        this.email = email;
        this.followerCount = followerCount;
        this.numPosts = numPosts;
        this.permissions = permissions;
    }


    /**
     * Getter and Setters below are technically redundant and can be removed.
     * They will be generated from the @Getter and @Setter tags above class
     */

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFollowerCount() {
        return this.followerCount;
    }

    public void setFollowerCount(String followerCount) {
        this.followerCount = followerCount;
    }

    public String getNumPosts() {
        return this.numPosts;
    }

    public void setNumPosts(String numPosts) {
        this.numPosts = numPosts;
    }

    public String getPermissions() {
        return this.permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return username + " "
               + email + " "
               + followerCount + " "
               + numPosts + " "
                + permissions;
    }
}
