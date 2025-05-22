package equifaxinterview.InterviewProject.controller;

import equifaxinterview.InterviewProject.domain.UserProfile;
import equifaxinterview.InterviewProject.service.UserProfileService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@Controller
@RequiredArgsConstructor
public class UserRestController {
    private final UserProfileService userProfileService;

    @PostMapping("/users")
    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile userProfile){
            return ResponseEntity.ok(userProfileService.saveUserProfile(userProfile));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfile>> getUsers(@RequestParam(name="search", required = false) String search){
        return ResponseEntity.ok(userProfileService.getUserProfiles(search));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfile> getUser(@PathVariable("id") String id){
        return ResponseEntity.ok(userProfileService.getUserProfile(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserProfile> updateUser(@PathVariable("id") String id, @RequestBody UserProfile userProfile){
        return ResponseEntity.ok(userProfileService.updateUserProfile(id, userProfile));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") String id){
        return ResponseEntity.ok(userProfileService.deleteUserProfile(id));
    }

}
