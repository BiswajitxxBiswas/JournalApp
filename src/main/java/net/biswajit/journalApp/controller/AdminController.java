package net.biswajit.journalApp.controller;

import net.biswajit.journalApp.cache.AppCache;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppCache appCache;

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(){
        List<User> list = userService.getAllUser();
        if(!list.isEmpty()){
            return new ResponseEntity<>(list, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-user-admin")
    public ResponseEntity<?> createUserAdmin(@RequestBody User user){
        try {
            userService.saveAdmin(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>("Failed to Create Admin "+e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/clear-cache")
    public void clearCache(){
        appCache.init();
    }

}
