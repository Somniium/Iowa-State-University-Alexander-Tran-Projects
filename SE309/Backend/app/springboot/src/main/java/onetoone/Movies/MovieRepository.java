package onetoone.Movies;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbId(String tmdbId);

    List<Movie> findByMediaTypeIgnoreCase(String mediaType);

    /**
     * Sorted, paged variant used by CyvalContextBuilder to fetch the top-N
     * movies/shows for a media type without pulling the whole catalog into memory.
     */
    List<Movie> findByMediaTypeIgnoreCaseOrderByRatingDesc(String mediaType, Pageable pageable);

    List<Movie> findByGenreIgnoreCase(String genre);

    Movie findByTitle(String title);

    @Transactional
    void deleteByTmdbId(String tmdbId);
}
