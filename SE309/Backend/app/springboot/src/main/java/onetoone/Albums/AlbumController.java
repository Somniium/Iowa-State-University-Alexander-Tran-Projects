package onetoone.Albums;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import onetoone.Artists.Artist;
import onetoone.Artists.ArtistRepository;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.List;
import onetoone.Reviews.Review;

@RestController
@Tag(name = "Albums", description = "Manage Albums using SpotifyAPI and database tools")
public class AlbumController {

    private final SpotifyService spotifyService;

    public AlbumController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    ArtistRepository artistRepository;

    @Operation(
            summary = "Returns all albums",
            description = "Returns all albums as JSON objects"
    )
    @GetMapping(path="/albums")
    List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    @Operation(
            summary = "Searches and adds album to DB",
            description = "Uses the SpotifyAPI to search for an album. Once found, it is saved to the DB and returned"
    )
    @GetMapping(path="/search-albums-and-save")
    ResponseEntity<?> searchForAndSaveAlbum(
            @Parameter(description = "Name of album to search for")
            @RequestParam(name = "album_name") String albumName) {
        try {
            spotifyService.authenticate();
            var searchRequest = spotifyService.getApi().searchAlbums(albumName).build();
            var results = searchRequest.execute();

            if (results.getItems() == null || results.getItems().length == 0) {
                return new ResponseEntity<>("No album found matching: " + albumName, HttpStatus.NOT_FOUND);
            }

            String spotifyId = results.getItems()[0].getId();

            Album existing = albumRepository.findBySpotifyId(spotifyId);
            if (existing != null) {
                return new ResponseEntity<>(existing, HttpStatus.OK);
            }

            String name = results.getItems()[0].getName();
            String releaseDate = results.getItems()[0].getReleaseDate();
            String coverURL = results.getItems()[0].getImages()[0].getUrl();

            // logic for extracting genre
            String artistId = results.getItems()[0].getArtists()[0].getId();

            Artist artist = artistRepository.findBySpotifyId(artistId);
            if (artist == null) {
                var artistRequest = spotifyService.getApi().getArtist(artistId).build().execute();

                String artistName = artistRequest.getName();
                String artistGenre = (artistRequest.getGenres().length > 0) ? artistRequest.getGenres()[0] : "Unknown";
                String artistURL = (artistRequest.getImages().length > 0) ? artistRequest.getImages()[0].getUrl() : null;

                artist = new Artist(artistName, artistGenre, artistURL, artistId);

                artist = artistRepository.save(artist);
            }

            Album album = new Album(name, releaseDate, coverURL, artist.getGenre(), spotifyId);

            artist.addAlbum(album);

            albumRepository.save(album);

            return new ResponseEntity<>(album, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching Spotify: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Returns album from DB",
            description = "Returns an album from the DB using the ID for the album"
    )
    @GetMapping(path="/album/{albumId}")
    ResponseEntity<?> getAlbum(
            @Parameter(description = "ID of album in DB", example = "1")
            @PathVariable Long albumId) {
        Album album = albumRepository.findByAlbumId(albumId);

        if(album == null) {
            return new ResponseEntity<>("Album not found under ID", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(album, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns artist from album from DB",
            description = "Returns an album from the DB using the ID for the album"
    )
    @GetMapping(path="/album/get-artist/{albumId}")
    ResponseEntity<?> getAlbumArtist(
            @Parameter(name = "albumId", description = "ID of album in DB")
            @PathVariable long albumId) {
        Album album = albumRepository.findByAlbumId(albumId);

        if(album == null) {
            return new ResponseEntity<>("Album not found under ID", HttpStatus.NOT_FOUND);
        }

        Artist artist = album.getArtist();

        if(artist == null) {
            return new ResponseEntity<>("Artist not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(artist, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns album by name",
            description = "Returns an album from the DB using the name of the album"
    )
    @GetMapping(path="/album/name")
    ResponseEntity<?> getAlbumByName(
            @Parameter(description = "Name of the album", example = "Flex Musix")
            @RequestParam(name="name") String name) {
        Album album = albumRepository.findByName(name);

        if(album == null) {
            return new ResponseEntity<>("Album not found under name", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(album, HttpStatus.OK);
    }

    @Operation(
            summary = "Adds album to DB",
            description = "Manually search for an add album to DB without returning"
    )
    @PostMapping(path="/add-album")
    ResponseEntity<?> addAlbum(
            @Parameter(description = "Name of the album", example = "Flex Musix")
            @RequestParam(name="album_name") String albumName) {
        try {
            spotifyService.authenticate();
            var searchRequest = spotifyService.getApi().searchAlbums(albumName).build();
            var results = searchRequest.execute();

            String spotifyId = results.getItems()[0].getId();
            String name = results.getItems()[0].getName();
            String artist = results.getItems()[0].getArtists()[0].getName();
            String releaseDate = results.getItems()[0].getReleaseDate();
            String coverURL = results.getItems()[0].getImages()[0].getUrl();

            // logic for extracting genre
            String artistId = results.getItems()[0].getArtists()[0].getId();
            var artistRequest = spotifyService.getApi().getArtist(artistId).build().execute();

            String genre = "Unknown";

            if (artistRequest.getGenres() != null && artistRequest.getGenres().length > 0) {
                genre = artistRequest.getGenres()[0];
            }

            Album album = new Album(name, releaseDate, coverURL, genre, spotifyId);

            albumRepository.save(album);

            return new ResponseEntity<>("Album saved.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: Album not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Delete album from DB",
            description = "Deletes the album from the DB using the ID of the album"
    )
    @Transactional
    @DeleteMapping(path="/album/{albumId}")
    ResponseEntity<?> deleteAlbumById(
            @Parameter(description = "ID of album in DB", example = "1")
            @PathVariable Long albumId) {
        albumRepository.deleteByAlbumId(albumId);
        return new ResponseEntity<>("Successfully deleted album.", HttpStatus.OK);
    }

    @Operation(
            summary = "Update an album",
            description = "Updates the album in the DB using the ID of the album and a JSON object"
    )
    @PutMapping(path="/album/{albumId}")
    ResponseEntity<?> updateAlbum(
            @Parameter(description = "ID of album in DB", example = "1")
            @PathVariable Long albumId,
            @RequestBody Album request) {
        Album existing = albumRepository.findByAlbumId(albumId);

        if(existing  == null) {
            return new ResponseEntity<>("Album id does not exist", HttpStatus.NOT_FOUND);
        }
        if (!request.getAlbumId().equals(albumId)){
            return new ResponseEntity<>("Path variable id does not match Album request id", HttpStatus.NOT_FOUND);
        }

        Album req = albumRepository.save(request);
        return new ResponseEntity<>(req, HttpStatus.OK);
    }

    @Operation(
            summary = "Update rating of album",
            description = "Updates the average rating of the album using ID of album"
    )
    @PutMapping(path="/album/update-average-rating/{albumId}")
    ResponseEntity<?> updateAlbumRating(
            @Parameter(description = "ID of album in DB", example = "1")
            @PathVariable long albumId) {
        Album album = albumRepository.findByAlbumId(albumId);

        if (album == null) {
            return new ResponseEntity<>("Album not found under ID", HttpStatus.NOT_FOUND);
        }

        int numRatings = 0;
        int sum = 0;

        for (Review r : album.getAlbumReviews()) {
            sum += r.getRating();
            numRatings++;
        }

        if (numRatings == 0) {
            return new ResponseEntity<>("No reviews to average", HttpStatus.OK);
        }

        album.setRating(sum / numRatings);
        album = albumRepository.save(album);

        return new ResponseEntity<>(album, HttpStatus.OK);
    }
}
