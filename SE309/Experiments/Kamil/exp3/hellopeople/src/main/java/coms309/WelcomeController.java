package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Welcome Controller that returns a string when user connects to CyVal
 *
 * @author Kamil Halupka
 */

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "If you are seeing this message, you have accessed CyVal via localhost!";
    }

    @GetMapping("/{username}")
    public String welcome(@PathVariable String username) {
        // This is the message that appears when you run the project and open http://localhost:8080/name
        return "Hello, " + username + "! If you are seeing this, you have accessed CyVal via localhost!";
    }
}

