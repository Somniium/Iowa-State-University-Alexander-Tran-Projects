package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        // This is the message that appears when you run the project and open http://localhost:8080/
        return "Hello and welcome to COMS 309! This is the text that appears when you" +
                " open local host in your favorite web browser!";
    }
    
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        // This is the message that appears when you run the project and open http://localhost:8080/name
        return "Hello and welcome to COMS 309: " + name + "! If you are seeing this, you have"
                + " succesfully ran the experiment with Maven!";
    }
}
