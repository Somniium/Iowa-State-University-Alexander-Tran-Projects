package onetoone.Admins;

import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;


public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByUser(User user);

    @Transactional
    void deleteByUser(User user);
}
