package onetoone.Posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Kamil Halupka
 * 
 */ 

public interface PostsRepository extends JpaRepository<Posts, Long> {
    Posts findById(int id);

    @Transactional
    void deleteById(int id);
}
