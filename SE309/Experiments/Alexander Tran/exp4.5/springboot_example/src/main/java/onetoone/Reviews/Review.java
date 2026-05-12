package onetoone.Reviews;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import onetoone.Users.User;

/**
 * 
 * @author Vivek Bengre
 */ 

@Entity
public class Review {
    
    /* 
     * The annotation @ID marks the field below as the primary key for the table created by springboot
     * The @GeneratedValue generates a value if not already present, The strategy in this case is to start from 1 and increment for each table
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    // MOVIE / SHOW / MUSIC
    private String mediaType;
    private String title;
    // 1–5 stars
    private int rating;

    @Column(length = 2000)
    private String body;
    /*
     * @OneToOne creates a relation between the current entity/table(Laptop) with the entity/table defined below it(Person)
     * @ManyToOne
     * @JsonIgnore is to assure that there is no infinite loop while returning either Person/laptop objects (laptop->Person->laptop->...)
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Review(String mediaType, String title, int rating, String body) {
        this.mediaType = mediaType;
        this.title = title;
        this.rating = rating;
        this.body = body;
    }

    public Review() {
    }

    // =============================== Getters and Setters for each field ================================== //

    public int getId() { return id; }
    public String getMediaType() { return mediaType; }
    public String getTitle() { return title; }
    public int getRating() { return rating; }
    public String getBody() { return body; }
    public User getUser() { return user; }

    public void setId(int id) { this.id = id; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public void setTitle(String title) { this.title = title; }
    public void setRating(int rating) { this.rating = rating; }
    public void setBody(String body) { this.body = body; }
    public void setUser(User user) { this.user = user; }
}
