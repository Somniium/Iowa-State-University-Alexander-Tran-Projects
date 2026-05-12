package onetoone.Comments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPost_IdOrderByCreatedAtAsc(int postId);

    List<Comment> findByAuthor_IdOrderByCreatedAtDesc(int authorId);

    long countByPost_Id(int postId);

    @Transactional
    void deleteByPost_Id(int postId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.author.userId = :authorId")
    int deleteByAuthorId(@Param("authorId") int authorId);
}
