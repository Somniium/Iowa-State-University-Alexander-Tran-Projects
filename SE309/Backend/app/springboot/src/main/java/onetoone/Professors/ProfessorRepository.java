package onetoone.Professors;

import onetoone.Admins.Admin;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Professor findByUser(User user);

    @Transactional
    void deleteByUser(User user);
}
