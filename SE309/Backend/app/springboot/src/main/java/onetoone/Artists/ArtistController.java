package onetoone.Artists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Albums.Album;
import onetoone.Albums.AlbumRepository;
import onetoone.Albums.SpotifyService;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;


@RestController
@Tag(name = "Artists", description = "Manage Artists using Spotify using database and SpotifyAPI")
public class ArtistController {

    private final SpotifyService spotifyService;

    public ArtistController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    AlbumRepository albumRepository;


    @Operation(
            summary = "Returns all artists",
            description = "Returns all artists as JSON objects"
    )
    @GetMapping(path = "/artists")
    List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }


    @Operation(
            summary = "Searches for an artist",
            description = "Searches for an artist. If found, the artist is saved to the DB"
    )
    @GetMapping(path = "/search-artist-and-save")
    ResponseEntity<?> searchForAndSaveArtist(
            @Parameter(description = "Name of the artist", example = "Kanye West")
            @RequestParam(name = "artist_name") String artistName) {
        try {
            spotifyService.authenticate();
            var searchRequest = spotifyService.getApi().searchArtists(artistName).build();
            var results = searchRequest.execute();

            String spotifyId = results.getItems()[0].getId();

            Artist existing = artistRepository.findBySpotifyId(spotifyId);

            if(existing != null) {
                return new ResponseEntity<>(existing, HttpStatus.OK);
            }

            String name = results.getItems()[0].getName();
            String photoURL = results.getItems()[0].getImages()[0].getUrl();
            String genre = "Unknown";

            if(results.getItems()[0].getGenres() != null && results.getItems()[0].getGenres().length > 0) {
                genre = results.getItems()[0].getGenres()[0];
            }

            Artist artist = new Artist(name, genre, photoURL, spotifyId);

            artistRepository.save(artist);

            return new ResponseEntity<>(artist, HttpStatus.OK);
        } catch(Exception e) {
            return new ResponseEntity<>("Error: Artist already in database.", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Get artist",
            description = "Get an artist using its ID in the DB",
            parameters = {
                @Parameter(name = "id", description = "ID of artist in DB.")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully returned artist."),
                @ApiResponse(responseCode = "404", description = "Artist not found under ID.")
            }
    )
    @GetMapping(path = "/artist/get-artist/{id}")
    ResponseEntity<?> getArtistById(@PathVariable long id) {
        Artist exists = artistRepository.findByArtistId(id);

        if(exists == null) {
            return new ResponseEntity<>("Artist not found under ID.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Adds all of artist's albums",
            description = "Retrieves all albums tied to an artist using artist's ID and adds them to the DB"
    )
    @PostMapping(path = "/add-artist-albums/id/{id}")
    ResponseEntity<?> addArtistAlbumsById(
            @Parameter(description = "ID of artist in DB", example = "1")
            @PathVariable Long id) {
        try {
            Artist artist = artistRepository.findByArtistId(id);

            String spotId = artist.getSpotifyId();

            spotifyService.authenticate();
            var searchRequest = spotifyService.getApi().getArtistsAlbums(spotId).build();
            var results = searchRequest.execute();

            for (int i = 0; i < results.getItems().length; i++) {
                String spotifyId = results.getItems()[i].getId();
                String albumName = results.getItems()[i].getName();
                String releaseDate = results.getItems()[i].getReleaseDate();
                String coverURL = results.getItems()[i].getImages()[0].getUrl();
                String genre = artist.getGenre();

                Album album = new Album(albumName, releaseDate, coverURL, genre, spotifyId);

                if(albumRepository.findBySpotifyId(spotifyId) != null) {
                    continue;
                }

                artist.addAlbum(album);
                albumRepository.save(album);
            }

            return new ResponseEntity<>("Artist albums saved.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: Artist already in database.", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Updates artist rating",
            description = "Updates average rating of artists using ratings of albums"
    )
    @PutMapping(path="/artist/update-rating/{id}")
    ResponseEntity<?> updateArtistRating(
            @Parameter(description = "ID of artist in DB", example = "1")
            @PathVariable Long id) {
        Artist existing = artistRepository.findByArtistId(id);

        if (existing == null) {
            return new ResponseEntity<>("Artist not found under ID", HttpStatus.NOT_FOUND);
        }

        if (existing.getAlbums() == null || existing.getAlbums().isEmpty()) {
            return new ResponseEntity<>("No albums to average", HttpStatus.OK);
        }

        int newRating = 0;
        for (int i = 0; i < existing.getAlbums().size(); i++) {
            newRating += existing.getAlbums().get(i).getRating();
        }

        newRating = newRating / existing.getAlbums().size();

        existing.setRating(newRating);
        artistRepository.save(existing);

        return new ResponseEntity<>(existing, HttpStatus.OK);
    }


    @Operation(
            summary = "Update an artist",
            description = "Updates an artist using ID of artist and JSON object"
    )
    @PutMapping(path = "/artist/update/{id}")
    ResponseEntity<?> updateArtist(
            @Parameter(description = "ID of artist in DB", example = "1")
            @PathVariable Long id,
            @RequestBody Artist request) {
        Artist existing = artistRepository.findByArtistId(id);

        if(existing  == null) {
            return new ResponseEntity<>("Artist id does not exist", HttpStatus.NOT_FOUND);
        }
        if (!request.getArtistId().equals(id)){
            return new ResponseEntity<>("Path variable id does not match Artist request id", HttpStatus.NOT_FOUND);
        }

        existing = artistRepository.save(request);
        return new ResponseEntity<>(existing, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete artist",
            description = "Deletes an artist from DB using ID of artist"
    )
    @Transactional
    @DeleteMapping(path="/artist/delete-artist/id/{id}")
    ResponseEntity<?> deleteArtistById(
            @Parameter(description = "ID of artist in DB", example = "1")
            @PathVariable Long id) {
        Artist existing = artistRepository.findByArtistId(id);

        albumRepository.deleteAll(existing.getAlbums());

        artistRepository.deleteByArtistId(id);

        return new ResponseEntity<>("Successfully removed artist", HttpStatus.OK);
    }
}
