package onetoone.Likes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {

    Optional<Like> findByUser_IdAndPost_Id(int userId, int postId);

    List<Like> findByPost_Id(int postId);

    List<Like> findByUser_Id(int userId);

    long countByPost_Id(int postId);

    boolean existsByUser_IdAndPost_Id(int userId, int postId);

    /**
     * Explicit DELETE so the SQL fires immediately instead of being buffered in the
     * persistence context. Returns rows-affected so callers can distinguish
     * "nothing to delete" from "deleted N".
     *
     * flushAutomatically  — flushes any pending changes before the DELETE runs.
     * clearAutomatically  — clears the persistence context after, so subsequent
     *                       queries (like countByPost_Id) read fresh DB state
     *                       rather than the stale Hibernate L1 cache.
     *
     * Without this, the previous derived deleteBy* method left the DELETE
     * unflushed long enough that countByPost_Id returned the pre-delete count,
     * so the response said {"liked":false,"count":<old number>}.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Like l WHERE l.user.userId = :userId AND l.post.id = :postId")
    int deleteByUser_IdAndPost_Id(@Param("userId") int userId, @Param("postId") int postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Like l WHERE l.post.id = :postId")
    int deleteByPost_Id(@Param("postId") int postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Like l WHERE l.user.userId = :userId")
    int deleteByUserId(@Param("userId") int userId);
}
