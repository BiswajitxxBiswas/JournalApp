package net.biswajit.journalApp.controller;


import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;


    @PostMapping
    public void createUser(@RequestBody User newUser){
        userService.saveNewUser(newUser);
    }

    @GetMapping("/health-check")
    public String healthCheck(){
        return "Ok";
    }
}
