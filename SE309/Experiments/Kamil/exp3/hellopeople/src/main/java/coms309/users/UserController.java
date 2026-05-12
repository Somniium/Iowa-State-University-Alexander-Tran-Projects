package coms309.users;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller used to create, list, modify, and delete users from the CyVal platform.
 *
 * @author Kamil Halupka
 */

@RestController
public class UserController {

    // HashMap used to store users (key = username)
    HashMap<String, User> userList = new  HashMap<>();

    /*
        Lists all users registered on CyVal.
     */
    @GetMapping("/users")
    public  HashMap<String, User> getAllUsers() {
        return userList;
    }

    /*
        Registers a new user to CyVal.
     */
    @PostMapping("/users")
    public  String createUser(@Valid @RequestBody User user) {
        System.out.println(user);

        String p = user.getPermissions();
        if(p == null || !(p.equals("admin")) && !(p.equals("moderator")) && !(p.equals("user"))) {
            return "Unsupported user type; accepted types:\n1. admin\n2. moderator\n3. user";
        }
        userList.put(user.getUsername(), user);
        String s = "New user "+ user.getUsername() + " Registered";
        return s;
    }

    /*
        List a user based on username given.
     */
    @GetMapping("/users/account/{username}")
    public User getUser(@PathVariable String username) {
        User u = userList.get(username);
        return u;
    }

    /*
        Lists all users based on what permissions they have.
     */
    @GetMapping("/users/permissions/{permissions}")
    public List<User> getPermissions(@PathVariable String permissions) {
        List<User> list = new ArrayList();
        for (User u : userList.values()) {
            if (u.getPermissions().equals(permissions)) {
                list.add(u);
            }
        }
        return list;
    }

    /*
        Used to update the information of a user based on username.
     */
    @PutMapping("/users/{username}")
    public User updateUser(@PathVariable String username, @RequestBody User u) {
        userList.replace(username, u);
        return userList.get(username);
    }


    /*
        Deletes a registered user from CyVal based on username.
     */
    @DeleteMapping("/users/{username}")
    public HashMap<String, User> deleteUser(@PathVariable String username) {
        userList.remove(username);
        return userList;
    }
    //Hello
}

