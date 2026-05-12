package onetoone;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;;import java.util.ArrayList;

@RestController
public class ReviewController {

    private final SpotifyService spotifyService;

    public ReviewController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/search-album")
    public String searchAlbum(@RequestParam(name = "album_name") String albumName) {
        try {
            spotifyService.authenticate();
            var searchRequest = spotifyService.getApi().searchAlbums(albumName).build();
            var results = searchRequest.execute();

            return "Found Album: " + results.getItems()[0].getName() + " by " + results.getItems()[0].getArtists()[0].getName();
        } catch (Exception e) {
            return "Search failed: " + e.getMessage();
        }
    }

    @GetMapping("/search-album-songlist")
    public String searchAlbumSonglist(@RequestParam(name = "album_name") String albumName) {
        try {
            spotifyService.authenticate();
            var searchRequest = spotifyService.getApi().searchAlbums(albumName).build();
            var results = searchRequest.execute();

            if (results.getItems().length > 0) {
                var album = results.getItems()[0];
                String albumID = album.getId();

                var tracksRequest = spotifyService.getApi().getAlbumsTracks(albumID).build();
                var tracksResults = tracksRequest.execute();

                ArrayList<String> songList = new ArrayList<>();
                for (var track : tracksResults.getItems()) {
                    songList.add(track.getName());
                }

                StringBuilder songs = new StringBuilder();
                for (int i = 0; i < songList.size(); i++) {
                    songs.append((i + 1) + ". " + songList.get(i) + "\n");
                }

                return "Found Album: " + results.getItems()[0].getName() + " by " + results.getItems()[0].getArtists()[0].getName() +
                        "\n\n" + songs.toString();
            }

            return "Album not found";
        } catch (Exception e) {
            return "Search failed: " + e.getMessage();
        }
    }

    @GetMapping("/search-artist")
    public String searchArtist(@RequestParam(name = "artist_name") String artistName) {
        try {
            spotifyService.authenticate();

            var searchRequest = spotifyService.getApi().searchArtists(artistName).build();
            var results = searchRequest.execute();

            return "Found Artist: " + results.getItems()[0].getName();

        } catch (Exception e) {
            return "Error: Artist not found";
        }
    }

    @GetMapping("/track-name")
    public String searchTrack(@RequestParam(name = "track_name") String trackName) {
        try {
            spotifyService.authenticate();

            var searchRequest = spotifyService.getApi().searchTracks(trackName).build();
            var results = searchRequest.execute();

            return "Found Track: " + results.getItems()[0].getName() + " by " + results.getItems()[0].getArtists()[0].getName();
        } catch (Exception e) {
            return "Error: Track not found";
        }
    }
}