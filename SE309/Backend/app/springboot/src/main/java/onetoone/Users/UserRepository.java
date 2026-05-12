package onetoone.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

public interface UserRepository extends JpaRepository<User, Integer> {

    //User findById(int id);

    User findByUserId(int userId);

    User findByEmailId(String emailId);

    User findByName(String name);

    @Transactional
    void deleteById(int id);

    @Transactional
    void deleteByEmailId(String emailId);
}
