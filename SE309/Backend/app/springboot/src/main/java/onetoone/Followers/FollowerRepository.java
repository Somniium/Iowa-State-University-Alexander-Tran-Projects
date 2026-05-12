package onetoone.Followers;

import jakarta.transaction.Transactional;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    Follower findById(long followerId);

    Follower findByFollowerUserIdAndFollowingUserId(int followerId, int followingId);

    @Query(
            "SELECT f.follower FROM Follower f WHERE f.following.userId = :userId AND f.isBlocked = false"
    )
    List<User> findFollowersByUserId(int userId);

    @Query(
            "SELECT f.following FROM Follower f WHERE f.follower.userId = :userId AND f.isBlocked = false"
    )
    List<User> findFollowingByUserId(int userId);

    // Returns following count
    int countByFollowerUserId(int followerUserId);

    // Returns follower count
    int countByFollowingUserId(int followingUserId);

    // Used to prevent duplicate follows
    //Follower findByFollowerUserIdAndFollowingUserId(int followerUserId, int followingUserId);

    @Transactional
    void deleteById(long followerId);

    @Transactional
    @Modifying
    @Query(
            "DELETE FROM Follower f WHERE f.follower.userId = :followerId AND f.following.userId = :followingId"
    )
    void deleteByUsers(int followerId, int followingId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "DELETE FROM Follower f WHERE f.follower.userId = :userId OR f.following.userId = :userId"
    )
    int deleteByUserId(@Param("userId") int userId);
}
