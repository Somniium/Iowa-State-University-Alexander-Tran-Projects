package onetoone.Reviews;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findById(int id);

    List<Review> findByMediaType(String mediaType);

    @Transactional
    void deleteById(int id);
}
