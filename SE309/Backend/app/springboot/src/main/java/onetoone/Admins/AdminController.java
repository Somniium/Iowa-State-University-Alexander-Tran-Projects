package onetoone.Admins;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.apache.coyote.Response;
import org.hibernate.annotations.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Tag(name = "Admins", description = "Manage admins and the users attached to them")
public class AdminController {
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    private UserRepository userRepository;

    @Operation(
            summary = "Returns all admins",
            description = "Returns all admins as JSON objects"
    )
    @GetMapping(path = "/admins")
    List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Operation(
            summary = "Return an admin by ID",
            description = "Returns an admin using ID of admin in DB"
    )
    @GetMapping(path = "/admin/id/{id}")
    ResponseEntity<?> getAdminById(
            @Parameter(description = "ID of admin", example = "1")
            @PathVariable Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);

        if(admin == null) {
            return new ResponseEntity<>("Admin not found under ID", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(admin, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns an admin by User",
            description = "Returns an admin using the user JSON object attached to it"
    )
    @GetMapping(path = "/admin/user/{userId}")
    ResponseEntity<?> getAdminByUser(
            @Parameter(description = "ID of user attached to admin", example = "1")
            @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        Admin admin = adminRepository.findByUser(user);

        if (admin == null) {
            return new ResponseEntity<>("Admin not found under user", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(admin, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns user of admin",
            description = "Returns the user attached to the admin using the ID of the admin"
    )
    @GetMapping(path = "/admin/get-user/{id}")
    ResponseEntity<?> getAdminUser(
            @Parameter(description = "ID of admin", example = "1")
            @PathVariable Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);

        if (admin == null) {
            return new ResponseEntity<>("Admin not found under ID", HttpStatus.NOT_FOUND);
        }

        User user = admin.getUser();

        if (user == null) {
            return new ResponseEntity<>("Admin user not found under ID", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(
            summary = "Adds an admin using ID",
            description = "Adds an admin using the DB ID of the user"
    )
    @PostMapping(path = "/admin/{id}")
    ResponseEntity<?> addAdminByUserId(
            @Parameter(description = "ID of user", example = "1")
            @PathVariable int id) {
        User user = userRepository.findByUserId(id);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (adminRepository.findByUser(user) != null) {
            return new ResponseEntity<>("User is already an admin.", HttpStatus.CONFLICT);
        }

        Admin admin = new Admin(user);
        adminRepository.save(admin);

        return new ResponseEntity<>(admin, HttpStatus.OK);
    }

    @Operation(
            summary = "Makes an admin a master user",
            description = "Makes an admin a master user using the DB ID of the admin"
    )
    @PutMapping(path = "/admin-make-master/{id}")
    ResponseEntity<?> makeAdminMaster(
            @Parameter(description = "ID of admin", example = "1")
            @PathVariable Long id) {
        Admin exists = adminRepository.findById(id).orElse(null);

        if(exists == null) {
            return new ResponseEntity<>("Admin not found under user", HttpStatus.NOT_FOUND);
        }

        exists.setMaster(true);
        adminRepository.save(exists);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Remove master user",
            description = "Removes a master user using the DB ID of the admin"
    )
    @PutMapping(path = "/admin-remove-master/{id}")
    ResponseEntity<?> removeMasterPerms(
            @Parameter(description = "ID of admin", example = "1")
            @PathVariable Long id) {
        Admin exists = adminRepository.findById(id).orElse(null);

        if(exists == null) {
            return new ResponseEntity<>("Admin not found under user", HttpStatus.NOT_FOUND);
        }

        exists.setMaster(false);
        adminRepository.save(exists);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Removes an admin by ID",
            description = "Removes and admin using the DB ID of the admin"
    )
    @Transactional
    @DeleteMapping(path = "/remove-admin/id/{id}")
    ResponseEntity<?> removeAdminById(
            @Parameter(description = "ID of admin", example = "1")
            @PathVariable Long id) {
        adminRepository.deleteById(id);
        return new ResponseEntity<>("Successfully removed admin.", HttpStatus.OK);
    }

    @Operation(
            summary = "Removes an admin by user",
            description = "Removes and admin using the user attached to it"
    )
    @Transactional
    @DeleteMapping(path = "/remove-admin/user/{userId}")
    ResponseEntity<?> removeAdminByUser(
            @Parameter(description = "ID of user attached to admin", example = "1")
            @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        adminRepository.deleteByUser(user);
        return new ResponseEntity<>("Successfully removed admin.", HttpStatus.OK);
    }
}
