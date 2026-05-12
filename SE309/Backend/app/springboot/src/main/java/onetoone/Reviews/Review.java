package onetoone.Reviews;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import onetoone.Albums.Album;
import onetoone.Books.Book;
import onetoone.Games.Game;
import onetoone.Movies.Movie;
import onetoone.Users.User;

@Entity
@Schema(description = "A user's review of any media item (album, book, game, movie, or show)")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique review ID", example = "1")
    private int id;

    @Schema(description = "Media category: ALBUM | BOOK | GAME | MOVIE | SHOW", example = "MOVIE")
    private String mediaType;

    @Schema(description = "Title of the reviewed item", example = "Inception")
    private String title;

    @Schema(description = "Metacritic-style score from 0 to 100", example = "92")
    private int rating;

    @Column(length = 2000)
    @Schema(description = "Full review text", example = "A mind-bending masterpiece.")
    private String body;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "album_id")
    @JsonIgnore
    private Album album;

    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private Book book;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @JsonIgnore
    private Game game;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private Movie movie;

    public Review(String mediaType, String title, int rating, String body) {
        this.mediaType = mediaType;
        this.title = title;
        this.rating = rating;
        this.body = body;
    }

    public Review() {}

    // Convenience fields so the FK IDs are visible in JSON responses
    @Schema(description = "ID of the author (User)", example = "42")
    public Integer getUserId()  { return user  != null ? user.getId()              : null; }
    @Schema(description = "ID of the linked Album", example = "3")
    public Long    getAlbumId() { return album != null ? album.getAlbumId()        : null; }
    @Schema(description = "ID of the linked Book",  example = "7")
    public Long    getBookId()  { return book  != null ? book.getId()              : null; }
    @Schema(description = "ID of the linked Game",  example = "2")
    public Long    getGameId()  { return game  != null ? game.getId()              : null; }
    @Schema(description = "ID of the linked Movie/Show", example = "5")
    public Long    getMovieId() { return movie != null ? movie.getId()             : null; }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
}
