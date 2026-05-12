package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Hello and welcome to CyVal!";
    }
    
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello and welcome to COMS 309: " + name;
    }

    @GetMapping("/posts")
    public String getPosts() {
        return "CyVal posts endpoint";
    }

    @GetMapping("/posts/{mediaType}")
    public String getPostsByType(@PathVariable String mediaType) {
        return "Showing CyVal posts for media type: " + mediaType;
    }
}
