package onetoone.Artists;

import onetoone.Albums.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Artist findByArtistId(Long artistId);

    Artist findByName(String name);

    Artist findBySpotifyId(String spotifyId);

    @Transactional
    @Modifying
    void deleteByArtistId(Long artistId);

    @Transactional
    void deleteByName(String name);
}