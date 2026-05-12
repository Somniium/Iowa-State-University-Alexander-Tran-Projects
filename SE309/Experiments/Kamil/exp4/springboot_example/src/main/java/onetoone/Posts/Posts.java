package onetoone.Posts;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Setter;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import onetoone.Users.User;

/**
 * 
 * @author Kamil Halupka
 */ 

@Entity
@Getter
@Setter
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String likes;
    private String reposts;
    private String shares;
    private boolean containsImage;

    @OneToOne
    @JsonIgnore
    private User User;

    public Posts(String likes, String reposts, String shares) {
        this.likes = likes;
        this.reposts = reposts;
        this.shares = shares;
        this.containsImage = true;
    }

    public Posts() {
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getReposts() {
        return reposts;
    }

    public void setReposts(String reposts) {
        this.reposts = reposts;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public User getUser() {
        return User;
    }

    public void setUser(User user) {
        User = user;
    }
}
