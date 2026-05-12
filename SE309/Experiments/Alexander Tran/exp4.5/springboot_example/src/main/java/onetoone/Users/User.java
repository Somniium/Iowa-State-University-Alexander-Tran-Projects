package onetoone.Users;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import onetoone.Reviews.Review;

/**
 * 
 * @author Vivek Bengre
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
    private int id;
    private String name;
     /*
     * Email annotation is part of the hibernate validator package that helps with validation of email
     * input. In which case, it currently is matching to the regexp that expresses any characters are allowed followed by an '@' except for '|' and '
     * as they are potential SQL injection risk. Flag here is used to discern that input is not case-sensitive.
     */
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String emailId;

    private boolean active = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    public User(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
        this.active = true;
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

    // =============================== Getters and Setters for each field ================================== //
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmailId() { return emailId; }
    public boolean isActive() { return active; }
    public List<Review> getReviews() { return reviews; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmailId(String emailId) { this.emailId = emailId; }
    public void setActive(boolean active) { this.active = active; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
