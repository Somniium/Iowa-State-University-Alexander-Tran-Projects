package onetoone.Group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import onetoone.Posts.Post;
import onetoone.Professors.Professor;
import onetoone.Professors.ProfessorRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Groups", description = "Manage groups and users that are in them.")
public class GroupController {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfessorRepository professorRepository;

    @Operation(
            summary = "Returns all groups",
            description = "Returns a list of JSON objects for every existing group.",
            parameters = {
            },
            responses = {
                     @ApiResponse(responseCode = "200", description = "Successfully returned group list.")
            }
    )
    @GetMapping(path = "/groups")
    List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Operation(
            summary = "Returns group by ID",
            description = "Returns group using the ID of the group in the database.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the group in the database.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned group."),
                    @ApiResponse(responseCode = "404", description = "Group not found in DB.")
            }
    )
    @GetMapping(path = "/group/id/{id}")
    ResponseEntity<?> getGroupById(@PathVariable Long id) {
        Group group = groupRepository.findByGroupId(id);

        if(group == null) {
            return new ResponseEntity<>("Group not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(group, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns group by Name",
            description = "Returns group using the name of the group in the database.",
            parameters = {
                    @Parameter(name = "name", description = "Name of the group in the database.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned group."),
                    @ApiResponse(responseCode = "404", description = "Group not found in DB.")
            }
    )
    @GetMapping(path = "/group/name/{name}")
    ResponseEntity<?> getGroupByName(@PathVariable String name) {
        Group group = groupRepository.findByName(name);

        if(group == null) {
            return new ResponseEntity<>("Group not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(group, HttpStatus.OK);
    }

    @Operation(
            summary = "Creates group",
            description = "Creates a new group using name, professor, and class.",
            parameters = {
                    @Parameter(name = "groupName", description = "Name of group."),
                    @Parameter(name = "profId", description = "ID of professor in DB."),
                    @Parameter(name = "className", description = "Name of class attached to group.")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created group."),
                    @ApiResponse(responseCode = "404", description = "Professor or Class not found.")
            }
    )
    @PostMapping(path = "/group/name/{groupName}/professor/{profId}/class/{className}")
    ResponseEntity<?> createGroup(@PathVariable String groupName, @PathVariable Long profId, @PathVariable String className) {
        Professor exists = professorRepository.findById(profId).orElse(null);

        if(exists == null) {
            return new ResponseEntity<>("Professor not found under ID.", HttpStatus.NOT_FOUND);
        }

        if(!exists.classExists(className)) {
           return new ResponseEntity<>("Class not found under name.", HttpStatus.NOT_FOUND);
        }

        Group group = new Group(groupName, className);
        group.setProfessor(exists);
        exists.getGroups().add(group);
        groupRepository.save(group);

        return new ResponseEntity<>(group, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Adds user to group",
            description = "Adds a user to a group using group and user IDs",
            parameters = {
                    @Parameter(name="groupId", description = "ID of group in DB."),
                    @Parameter(name="userId" , description = "ID of user in DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully added user to group."),
                    @ApiResponse(responseCode = "404", description = "Group or User not found.")
            }
    )
    @PostMapping(path = "/add-user-to-group/group/{groupId}/user/{userId}")
    ResponseEntity<?> addUserToGroup(@PathVariable Long groupId, @PathVariable int userId) {
        Group exists = groupRepository.findByGroupId(groupId);
        User user = userRepository.findByUserId(userId);

        if(exists == null) {
            return new ResponseEntity<>("Group not found under ID.", HttpStatus.NOT_FOUND);
        }

        if(user == null) {
            return new ResponseEntity<>("Professor not found under ID.", HttpStatus.NOT_FOUND);
        }

        if(exists.getMembers().contains(user)) {
            return new ResponseEntity<>("User already in group.", HttpStatus.CONFLICT);
        }

        exists.addMember(user);
        groupRepository.save(exists);

        return new ResponseEntity<>("User added to group.", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Updates group",
            description = "Updates group using ID of group in DB and group JSON body.",
            parameters = {
                    @Parameter(name="id", description = "ID of group in DB."),
                    @Parameter(name="request", description = "Updated group object JSON body.")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated the group."),
                    @ApiResponse(responseCode = "404", description = "Group not found.")
            }
    )
    @PutMapping(path="/update-group/{id}")
    ResponseEntity<?> updateGroup(@PathVariable Long id, @RequestBody Group request) {
        Group exists = groupRepository.findByGroupId(id);

        if(exists == null) {
            return new ResponseEntity<>(Map.of("message", "Group not found under ID."), HttpStatus.NOT_FOUND);
        }

        groupRepository.save(request);

        return new ResponseEntity<>(Map.of("message", "Successfully updated group."), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a group",
            description = "Deletes a group from the DB using the group's ID",
            parameters = {
                    @Parameter(name="id", description = "ID of the group in the DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted the group."),
                    @ApiResponse(responseCode = "404", description = "Group not found.")
            }
    )
    @DeleteMapping(path="/delete-group/{id}")
    ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        Group exists = groupRepository.findByGroupId(id);

        if(exists == null) {
            return new ResponseEntity<>("Group not found under ID.", HttpStatus.NOT_FOUND);
        }

        groupRepository.deleteByGroupId(id);

        return new ResponseEntity<>("Successfully deleted group.", HttpStatus.OK);
    }

    @Operation(
            summary = "Remove a user",
            description = "Removes a user from the group using the group's ID",
            parameters = {
                    @Parameter(name="id", description = "ID of the group in the DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully removed the user."),
                    @ApiResponse(responseCode = "404", description = "Group not found.")
            }
    )
    @DeleteMapping(path="/remove-user-from-group/group/{groupId}/user/{userId}")
    ResponseEntity<?> deleteUserFromGroup(@PathVariable Long groupId, @PathVariable int userId) {
        Group exists = groupRepository.findByGroupId(groupId);
        User user = userRepository.findByUserId(userId);

        if(exists == null) {
            return new ResponseEntity<>("Group not found under ID.", HttpStatus.NOT_FOUND);
        }

        if(user == null) {
            return new ResponseEntity<>("Professor not found under ID.", HttpStatus.NOT_FOUND);
        }

        if(!exists.getMembers().contains(user)) {
            return new ResponseEntity<>("User not in group.", HttpStatus.NOT_FOUND);
        }

        exists.removeMember(user);
        groupRepository.save(exists);

        return new ResponseEntity<>("User removed from group.", HttpStatus.OK);
    }
}
