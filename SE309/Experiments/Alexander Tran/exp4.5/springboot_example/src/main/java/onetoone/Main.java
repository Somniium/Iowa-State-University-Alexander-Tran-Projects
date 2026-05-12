package onetoone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import onetoone.Reviews.Review;
import onetoone.Users.User;
import onetoone.Users.UserRepository;

/**
 * 
 * @author Vivek Bengre
 * 
 */ 

@SpringBootApplication
class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Create 3 Persons with their machines
    /**
     * 
     * @param userRepository for the User entity
     * Creates a commandLine runner to enter dummy data into the database
     * As mentioned in User.java just associating the Laptop object with the Person will save it into the database because of the CascadeType
     */

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            User u1 = new User("Alex Tran", "trana25@iastate.edu");
            User u2 = new User("Jamie", "jamie@iastate.edu");

            // reviews (movie/show/music)
            u1.addReview(new Review("MOVIE", "Whiplash", 5, "Insane pacing and editing. Loved it."));
            u1.addReview(new Review("MUSIC", "Deftones - White Pony", 5, "Timeless. Atmosphere is unreal."));
            u2.addReview(new Review("SHOW", "The Bear", 4, "Stressful but super good."));

            userRepository.save(u1);
            userRepository.save(u2);
        };
    }

}
