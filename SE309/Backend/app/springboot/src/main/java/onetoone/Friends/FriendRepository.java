package onetoone.Friends;

import jakarta.transaction.Transactional;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.user.userId = :userId AND f.friend.userId = :friendId) OR " +
           "(f.user.userId = :friendId AND f.friend.userId = :userId)")
    Friend findFriendBetweenUsers(@Param("userId") int userId, @Param("friendId") int friendId);

    List<Friend> findByUserUserId(int userId);

    List<Friend> findByFriendUserId(int userId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Friend f WHERE f.user.userId = :userId OR f.friend.userId = :userId")
    int deleteByUserId(@Param("userId") int userId);
}
