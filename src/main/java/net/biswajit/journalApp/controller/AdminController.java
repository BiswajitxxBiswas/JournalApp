package net.biswajit.journalApp.controller;

import net.biswajit.journalApp.cache.AppCache;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Admin functionalities such as fetching all users, creating an admin user, and clearing cache.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppCache appCache;

    /**
     * Fetches all registered users in the system.
     *
     */
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(){
        List<User> list = userService.getAllUser();
        if(!list.isEmpty()){
            return new ResponseEntity<>(list, HttpStatus.OK);
        }
        return new ResponseEntity<>("No users found.", HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a new admin user.
     *
     */
    @PostMapping("/create-user-admin")
    public ResponseEntity<?> createUserAdmin(@RequestBody User user){
        try {
            userService.saveAdmin(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>("Failed to Create Admin "+e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Clears the application cache.
     * This helps in refreshing stored data.
     */
    @GetMapping("/clear-cache")
    public ResponseEntity<?> clearCache(){
        try {
            appCache.init();
            return new ResponseEntity<>("Cache cleared successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to clear cache: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
