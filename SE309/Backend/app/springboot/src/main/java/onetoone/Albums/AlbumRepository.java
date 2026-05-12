package onetoone.Albums;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
/**
 *
 * @author Kamil Halupka
 *
 */

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Album findByAlbumId(Long albumId);

    Album findByName(String name);

    Album findBySpotifyId(String spotifyId);

    @Transactional
    @Modifying
    void deleteByAlbumId(Long albumId);

    @Transactional
    void deleteByName(String name);
}