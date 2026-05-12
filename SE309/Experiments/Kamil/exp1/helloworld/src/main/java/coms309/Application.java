package coms309;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import java.util.Arrays;

/**
 * PetClinic Spring Boot Application.
 * 
 * @author Vivek Bengre
 */

@SpringBootApplication
public class Application {
	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    // Found this code snippet here: https://spring.io/guides/gs/spring-boot
    // Lists all the beans that are part of the project using SpringBoot after it is built with Maven
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Here is a list of beans in project provided by SpringBoot: ");

            String[] beans = ctx.getBeanDefinitionNames();

            // Alphabetically sorts all beans in the list
            Arrays.sort(beans);

            // Prints out all bean names in the Array
            for (String beanName : beans) {
                System.out.println(beanName);
            }

        };
    }
}
