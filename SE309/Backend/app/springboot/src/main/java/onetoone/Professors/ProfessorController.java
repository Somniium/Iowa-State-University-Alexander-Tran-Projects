package onetoone.Professors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Group.Group;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name="Professors", description = "Manage professors and users tied to them.")
public class ProfessorController {
    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    UserRepository userRepository;

    @Operation(
            summary = "Return all professors",
            description = "Returns all professors as JSON objects"
    )
    @GetMapping(path = "/professors")
    List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }


    @Operation(
            summary = "Return a professor by ID",
            description = "Returns a professor using its DB ID"
    )
    @GetMapping(path = "/professor/id/{id}")
    ResponseEntity<?> getProfessorById(
            @Parameter(description = "ID of the professor", example = "1")
            @PathVariable Long id) {
        Professor prof = professorRepository.findById(id).orElse(null);

        if(prof == null) {
            return new ResponseEntity<>("Professor not found under ID", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prof, HttpStatus.OK);
    }

    @Operation(
            summary = "Return a professor by user",
            description = "Returns a professor using the user attached to it"
    )
    @GetMapping(path = "/professor/user/{userId}")
    ResponseEntity<?> getProfessorByUser(
            @Parameter(description = "ID of user attached to the professor", example = "1")
            @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        Professor prof = professorRepository.findByUser(user);

        if (prof == null) {
            return new ResponseEntity<>("Professor not found under user", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prof, HttpStatus.OK);
    }

    @Operation(
            summary = "Return a professor's groups",
            description = "Returns all of the groups that a professor owns.",
            parameters = {
                    @Parameter(name = "id", description = "ID of professor in DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Groups returned successfully"),
                    @ApiResponse(responseCode = "404", description = "Professor not found under ID")
            }
    )
    @GetMapping(path="/get-professor-groups/{id}")
    ResponseEntity<?> getProfessorGroups(@PathVariable Long id) {
        Professor professor = professorRepository.findById(id).orElse(null);

        if(professor == null) {
            return new ResponseEntity<>("Professor not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(professor.getGroups(), HttpStatus.OK);
    }

    @Operation(
            summary = "Returns user for professor",
            description = "Returns the user object that is attached to the professor"
    )
    @GetMapping(path = "/professor/get-user/{id}")
    ResponseEntity<?> getProfessorUser(
            @Parameter(description = "ID of the professor", example = "1")
            @PathVariable Long id) {
        Professor prof = professorRepository.findById(id).orElse(null);

        if (prof == null) {
            return new ResponseEntity<>("Professor not found under ID", HttpStatus.NOT_FOUND);
        }

        User user = prof.getUser();

        if (user == null) {
            return new ResponseEntity<>("Professor user not found under ID", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Operation(
            summary = "Adds a professor",
            description = "Adds a professor using the ID of attached user"
    )
    @PostMapping(path = "/professor/{id}")
    ResponseEntity<?> addProfessorByUserId(
            @Parameter(description = "ID of the user", example = "1")
            @PathVariable int id) {
        User user = userRepository.findByUserId(id);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Professor prof = new Professor(user);
        professorRepository.save(prof);

        return new ResponseEntity<>(prof, HttpStatus.OK);
    }

    @Operation(
            summary = "Adds class to professor",
            description = "Adds a class to the list of classes that a professor is teaching"
    )
    @PutMapping(path = "/professor/add-class/{id}/{className}")
    ResponseEntity<?> addClassToProfessor(
            @Parameter(description = "ID of the professor", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Name of the class", example = "COMS309")
            @PathVariable String className) {
        Professor exists = professorRepository.findById(id).orElse(null);

        if(exists == null) {
            return new ResponseEntity<>("Professor not found under user", HttpStatus.NOT_FOUND);
        }

        exists.addClass(className);
        professorRepository.save(exists);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Remove a class from professor",
            description = "Removes a class from the list of classes the professor teaches"
    )
    @PutMapping(path = "/professor/remove-class/{id}/{className}")
    ResponseEntity<?> removeClassFromProfessor(
            @Parameter(description = "ID of the professor", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Name of the class", example = "COMS309")
            @PathVariable String className) {
        Professor exists = professorRepository.findById(id).orElse(null);

        if(exists == null) {
            return new ResponseEntity<>("Professor not found under user", HttpStatus.NOT_FOUND);
        }

        exists.removeClass(className);
        professorRepository.save(exists);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(
            summary = "Remove professor by ID",
            description = "Removes a professor using the DB ID of the professor"
    )
    @Transactional
    @DeleteMapping(path="/professor/remove-professor/id/{id}")
    ResponseEntity<?> removeProfessorById(
            @Parameter(description = "ID of the professor", example = "1")
            @PathVariable Long id) {
        Professor professor = professorRepository.findById(id).orElse(null);

        if(professor == null) {
            return new ResponseEntity<>("Professor not found under ID", HttpStatus.NOT_FOUND);
        }

        deleteProfessorAndReferences(professor);
        return new ResponseEntity<>("Successfully removed professor.", HttpStatus.OK);
    }

    @Operation(
            summary = "Remove professor by User",
            description = "Removes a professor using the user attached to it"
    )
    @Transactional
    @DeleteMapping(path="/professor/remove-professor/user/{userId}")
    ResponseEntity<?> removeProfessorByUser(
            @Parameter(description = "ID of user attached to the professor", example = "1")
            @PathVariable int userId) {
        User user = userRepository.findByUserId(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        Professor professor = professorRepository.findByUser(user);

        if (professor == null) {
            return new ResponseEntity<>("Professor not found under user", HttpStatus.NOT_FOUND);
        }

        deleteProfessorAndReferences(professor);
        return new ResponseEntity<>("Successfully removed professor.", HttpStatus.OK);
    }

    private void deleteProfessorAndReferences(Professor professor) {
        for (Group group : new ArrayList<>(professor.getGroups())) {
            group.setProfessor(null);
        }
        professor.getGroups().clear();
        professorRepository.delete(professor);
    }
}
