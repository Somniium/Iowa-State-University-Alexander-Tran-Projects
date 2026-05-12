package onetoone.Books;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByVolumeId(String volumeId);
    Book findByTitle(String title);
}