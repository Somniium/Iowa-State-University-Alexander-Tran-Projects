package coms309.people;

import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */
/*
@RestController
public class PeopleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Person> peopleList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public  HashMap<String,Person> getAllPersons() {
        return peopleList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public  String createPerson(@RequestBody Person person) {
        System.out.println(person);
        peopleList.put(person.getFirstName(), person);
        String s = "New person "+ person.getFirstName() + " Saved";
        return s;
        //public  ResponseEntity<Map<String, String>>  //unused
        // createPerson(@RequestBody Person person) { // unused
        //Map <String, String> body = new HashMap<>();// unused
        //body.put("message", s); // unused
        //ResponseEntity<>(body, HttpStatus.OK); // unused
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // Note: To READ we use GET method
    @GetMapping("/people/{firstName}")
    public Person getPerson(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p;
    }

    // THIS IS A GET METHOD
    // RequestParam is expected from the request under the key "name"
    // returns all names that contains value passed to the key "name"
    @GetMapping("/people/contains")
    public List<Person> getPersonByParam(@RequestParam("name") String name) {
        List<Person> res = new ArrayList<>(); 
        for (Person p : peopleList.values()) {
            if (p.getFirstName().contains(name) || p.getLastName().contains(name))
                res.add(p);
        }
        return res;
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // Note: To UPDATE we use PUT method
    @PutMapping("/people/{firstName}")
    public Person updatePerson(@PathVariable String firstName, @RequestBody Person p) {
        peopleList.replace(firstName, p);
        return peopleList.get(firstName);
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/people/{firstName}")
    public HashMap<String, Person> deletePerson(@PathVariable String firstName) {
        peopleList.remove(firstName);
        return peopleList;
    }
} // end of people controller
*/

/**
 * CyVal Customization
 *
 * Author: Alexander Tran
 *
 * (This is basically just what is above but repurposed into something more fitting for CyVal)
 */
@RestController
public class PostsController {
    private final HashMap<String, Post> posts = new HashMap<>();

    // Seed a few posts so GET /posts shows something immediately
    public PostsController() {
        posts.put("Favorite albums this week?",
                new Post(
                        "Favorite albums this week?",
                        "Drop recs + why you like them.",
                        "MUSIC",
                        "Alexander Tran",
                        "PUBLIC"
                ));
        posts.put("Paper discussion: distributed systems",
                new Post(
                        "Paper discussion: distributed systems",
                        "What was the main takeaway for you?",
                        "ACADEMIC_PAPER",
                        "isuStudent42",
                        "PUBLIC"
                ));
    }

    // LIST (GET) - return all posts as JSON
    @GetMapping("/posts")
    public HashMap<String, Post> getAllPosts() {
        return posts;
    }

    // CREATE (POST) - create a new post from JSON body
    @PostMapping("/posts")
    public String createPost(@RequestBody Post post) {
        System.out.println(post);
        posts.put(post.getTitle(), post);
        return "New post '" + post.getTitle() + "' saved";
    }

    // READ (GET) - read one post by title
    @GetMapping("/posts/{title}")
    public Post getPost(@PathVariable String title) {
        return posts.get(title);
    }

    // SEARCH (GET) - query param search like  
    @GetMapping("/posts/contains")
    public List<Post> searchPosts(@RequestParam("query") String query) {
        List<Post> res = new ArrayList<>();
        String q = query.toLowerCase();

        for (Post p : posts.values()) {
            if ((p.getTitle() != null && p.getTitle().toLowerCase().contains(q)) ||
                    (p.getBody() != null && p.getBody().toLowerCase().contains(q)) ||
                    (p.getMediaType() != null && p.getMediaType().toLowerCase().contains(q)) ||
                    (p.getAuthor() != null && p.getAuthor().toLowerCase().contains(q))) {
                res.add(p);
            }
        }
        return res;
    }

    // UPDATE (PUT) - replace the stored post under the given title key
    @PutMapping("/posts/{title}")
    public Post updatePost(@PathVariable String title, @RequestBody Post updated) {
        posts.replace(title, updated);
        return posts.get(title);
    }

    // DELETE (DELETE) - remove the post with key {title}
    @DeleteMapping("/posts/{title}")
    public HashMap<String, Post> deletePost(@PathVariable String title) {
        posts.remove(title);
        return posts;
    }
}
