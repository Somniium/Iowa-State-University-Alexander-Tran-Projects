package onetoone.Users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import onetoone.Posts.Posts;
import onetoone.Posts.PostsRepository;

/**
 * 
 * @author Kamil Halupka
 * 
 */ 

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostsRepository postsRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/Users")
    List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping(path = "/Users/{id}")
    User getUserById( @PathVariable int id){
        return userRepository.findById(id);
    }

    @PostMapping(path = "/Users")
    String createUser(@RequestBody User User) {
        if (User == null)
            return failure;
        userRepository.save(User);
        return success;
    }

    @PutMapping("/Users/{id}")
    User updateUser(@PathVariable int id, @RequestBody User request){
        User User = userRepository.findById(id);

        if(User == null) {
            throw new RuntimeException("User id does not exist");
        }
        else if (User.getId() != id){
            throw new RuntimeException("path variable id does not match User request id");
        }

        userRepository.save(request);
        return userRepository.findById(id);
    }

    @PutMapping("/Users/{UserId}/Posts/{PostsId}")
    String assignPostsToUser(@PathVariable int UserId,@PathVariable int PostsId){
        User User = userRepository.findById(UserId);
        Posts Posts = postsRepository.findById(PostsId);
        if(User == null || Posts == null)
            return failure;
        Posts.setUser(User);
        User.setPost(Posts);
        userRepository.save(User);
        return success;
    }

    @DeleteMapping(path = "/Users/{id}")
    String deleteUser(@PathVariable int id){
        userRepository.deleteById(id);
        return success;
    }
}
