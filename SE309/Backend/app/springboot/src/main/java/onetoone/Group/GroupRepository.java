package onetoone.Group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findByGroupId(Long groupId);

    Group findByName(String name);

    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.messages m " +
            "LEFT JOIN FETCH m.user " +
            "LEFT JOIN FETCH g.members " +
            "WHERE g.groupId = :groupId")
    Group findByGroupIdWithDetails(@Param("groupId") Long groupId);

    @Transactional
    @Modifying
    void deleteByGroupId(Long groupId);

    @Transactional
    @Modifying
    void deleteByName(String name);
}
