package onetoone.Albums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Artists.Artist;
import onetoone.Reviews.Review;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="album_id")
    private Long albumId;

    @Column(unique = true)
    private String spotifyId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    @JsonIgnore
    private Artist artist;

    private String releaseDate;

    private String coverURL;

    private String genre;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> albumReviews = new ArrayList<>();

    private int rating = 0;

    public Album(String name, String releaseDate, String coverURL, String genre, String spotifyId) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.coverURL = coverURL;
        this.genre = genre;
        this.spotifyId = spotifyId;
    }

    public Album() {
    }

    // Getters and Setters

    public void addReview(Review review) {
        this.albumReviews.add(review);
        review.setAlbum(this);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<Review> getAlbumReviews() {
        return albumReviews;
    }

    public void setAlbumReviews(List<Review> albumReviews) {
        this.albumReviews = albumReviews;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSpotifyId() { return spotifyId; }

    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }

}

