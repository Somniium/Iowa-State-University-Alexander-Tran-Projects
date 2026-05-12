package onetoone.Games;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByRawgId(String rawgId);

    Game findByTitle(String title);

    List<Game> findByGenreIgnoreCase(String genre);

    @Transactional
    void deleteByRawgId(String rawgId);
}
