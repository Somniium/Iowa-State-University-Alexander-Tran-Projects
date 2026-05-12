package onetoone.Posts;

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

import onetoone.Users.User;
import onetoone.Users.UserRepository;


/**
 * 
 * @author Kamil Halupka
 * 
 */ 

@RestController
public class PostsController {

    @Autowired
    PostsRepository postsRepository;

    @Autowired
    UserRepository userRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @GetMapping(path = "/Posts")
    List<Posts> getAllPosts(){
        return postsRepository.findAll();
    }

    @GetMapping(path = "/Posts/{id}")
    Posts getPostById(@PathVariable int id){
        return postsRepository.findById(id);
    }

    @PostMapping(path = "/Posts")
    String createPost(@RequestBody Posts Post){
        if (Post == null)
            return failure;
        postsRepository.save(Post);
        return success;
    }

    @PutMapping(path = "/Posts/{id}")
    Posts updatePost(@PathVariable int id, @RequestBody Posts request){
        Posts Post = postsRepository.findById(id);
        if(Post == null)
            return null;
        postsRepository.save(request);
        return postsRepository.findById(id);
    }

    @DeleteMapping(path = "/Posts/{id}")
    String deletePost(@PathVariable int id){

        User User = userRepository.findByPost_Id(id);
        User.setPost(null);
        userRepository.save(User);

        postsRepository.deleteById(id);
        return success;
    }
}
