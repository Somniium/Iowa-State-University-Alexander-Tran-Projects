package onetoone.Artists;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import onetoone.Albums.Album;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="artist_id")
    private Long artistId;

    @Column(unique = true)
    private String spotifyId;

    private String name;

    private String genre;

    private String photoURL;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> albums = new ArrayList<>();

    private int rating = 0;

    public Artist(String name, String genre, String photoURL, String spotifyId) {
        this.name = name;
        this.genre = genre;
        this.photoURL = photoURL;
        this.spotifyId = spotifyId;
    }

    public Artist() {}

    public void addAlbum(Album album) {
        this.albums.add(album);
        album.setArtist(this);
    }
}
