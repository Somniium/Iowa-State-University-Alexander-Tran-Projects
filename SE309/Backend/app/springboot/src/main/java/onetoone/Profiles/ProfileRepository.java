package onetoone.Profiles;

import jakarta.transaction.Transactional;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByProfileId(int profileId);

    Profile findByUser(User user);

    @Query(value = "select p.* from profile p inner join users u on p.user_id = u.id", nativeQuery = true)
    List<Profile> findAllWithExistingUser();

    @Query(value = "select p.* from profile p inner join users u on p.user_id = u.id where p.user_id = :profileId", nativeQuery = true)
    Profile findByProfileIdWithExistingUser(@Param("profileId") int profileId);

    @Transactional
    void deleteByProfileId(int id);
}
