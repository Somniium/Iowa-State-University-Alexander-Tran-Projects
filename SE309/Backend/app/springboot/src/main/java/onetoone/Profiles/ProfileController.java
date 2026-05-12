package onetoone.Profiles;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import onetoone.Professors.Professor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Profiles", description = "Manage user profiles")
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Operation(
            summary = "Return all profiles.",
            description = "Returns a list of all profiles in DB."
    )
    @GetMapping(path = "/profiles")
    List<Profile> getAllProfiles() {
        return profileRepository.findAllWithExistingUser();
    }

    @Operation(
            summary = "Return a profile",
            description = "Returns a profile using ID of profile in DB.",
            parameters = {
                @Parameter(name = "id", description = "ID of profile in DB")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully returned profile"),
                @ApiResponse(responseCode = "404", description = "Profile was not found under ID.")
            }
    )
    @GetMapping(path="/profile/profileId/{id}")
    ResponseEntity<?> getProfileById(@PathVariable int id) {
        Profile prof = profileRepository.findByProfileIdWithExistingUser(id);

        if(prof == null) {
            return new ResponseEntity<>("Profile not found under ID", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(prof, HttpStatus.OK);
    }

    @Operation(
            summary = "Return a profile by user ID",
            description = "Returns a profile using ID of user in DB.",
            parameters = {
                    @Parameter(name = "id", description = "ID of user in DB")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully returned profile"),
                    @ApiResponse(responseCode = "404", description = "Profile or user was not found under ID.")
            }
    )
    @GetMapping(path="/profile/userId/{id}")
    ResponseEntity<?> getProfileByUserId(@PathVariable int id) {
        User exists = userRepository.findByUserId(id);

        if(exists == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        Profile prof = profileRepository.findByUser(exists);

        if(prof == null) {
            return new ResponseEntity<>("Profile not found under user", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(prof, HttpStatus.OK);
    }

    @Operation(
            summary = "Create a profile",
            description = "Creates a profile using ID of user in DB",
            parameters = {
                @Parameter(name = "id", description = "ID of user in DB.")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created profile"),
                    @ApiResponse(responseCode = "404", description = "User was not found under ID.")
            }
    )
    @PostMapping(path = "/profile/userId/{id}")
    ResponseEntity<?> createProfileForUser(@PathVariable int id, @RequestBody Profile request) {
        User exists = userRepository.findByUserId(id);

        if(exists == null) {
            return new ResponseEntity<>("User not found under ID", HttpStatus.NOT_FOUND);
        }

        request.setUser(exists);

        Profile prof = profileRepository.save(request);

        return new ResponseEntity<>(prof, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update a profile.",
            description = "Update a profile using the ID of the profile in the DB.",
            parameters = {
                @Parameter(name="id", description = "Id of the profile in the DB.")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successfully updated profile."),
                @ApiResponse(responseCode = "404", description = "Profile not found under ID")
            }
    )
    @PutMapping(path = "/update-profile/{id}")
    ResponseEntity<?> updateProfile(@PathVariable int id, @RequestBody Profile request) {
        Profile exists = profileRepository.findByProfileIdWithExistingUser(id);

        if(exists == null) {
            return new ResponseEntity<>("Profile not found under ID", HttpStatus.NOT_FOUND);
        }

        if(request.getBio() != null) {
            exists.setBio(request.getBio());
        }
        if(request.getMajor() != null) {
            exists.setMajor(request.getMajor());
        }
        if(request.getHobbies() != null) {
            exists.setHobbies(request.getHobbies());
        }
        if(request.getGradDate() != null) {
            exists.setGradDate(request.getGradDate());
        }
        if(request.getLinkedInURL() != null) {
            exists.setLinkedInURL(request.getLinkedInURL());
        }

        Profile prof = profileRepository.save(exists);

        return new ResponseEntity<>(prof, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a profile",
            description = "Deletes a profile using the ID of the profile in the DB",
            parameters = {
                @Parameter(name="id", description = "ID of the profile in the DB")
            },
            responses = {
                @ApiResponse(responseCode = "204", description = "Profile has been deleted"),
                @ApiResponse(responseCode = "404", description = "Profile not found under ID")
            }
    )
    @DeleteMapping(path = "/delete-profile/{id}")
    ResponseEntity<?> deleteProfile(@PathVariable int id) {
        Profile exists = profileRepository.findByProfileId(id);

        if(exists == null) {
            return new ResponseEntity<>("Profile not found under ID", HttpStatus.NOT_FOUND);
        }

        exists.getUser().setProfile(null);

        profileRepository.deleteByProfileId(id);

        return new ResponseEntity<>("Profile successfully deleted", HttpStatus.NO_CONTENT);
    }
}
