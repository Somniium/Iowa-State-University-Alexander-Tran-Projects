package org.springframework.samples.petclinic.system;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return """
        Welcome to CyVal <br/>
        Visit <a href="/post/create">/post/create</a> to create sample posts<br/>
        Visit <a href="/posts">/posts</a> to view all posts
        """;
    }
}
