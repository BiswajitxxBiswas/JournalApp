package net.biswajit.journalApp.controller;


import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.dto.UpdateCredentialsDTO;
import net.biswajit.journalApp.dto.UserDTO;
import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.api.response.WeatherResponse;
import net.biswajit.journalApp.repository.JournalEntryRepository;
import net.biswajit.journalApp.repository.UserRepository;
import net.biswajit.journalApp.service.UserService;
import net.biswajit.journalApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    /*
    * Get User Details
    * */
    @GetMapping
    public ResponseEntity<?> getUser(){

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            User user = userRepository.findByUserName(userName);

            if(user != null){
                return new ResponseEntity<>(user,HttpStatus.OK);
            }

            return new ResponseEntity<>("Error Fetching User",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Update user details
     */
    @Transactional
    @PutMapping("/update-user")
    public ResponseEntity<?> updateUser(@RequestBody UpdateCredentialsDTO credentialsDTO) {
        log.info("Credentials {}",credentialsDTO);
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            log.info("Current User {}",currentUsername);

            User user = userService.findByUserName(currentUsername);
            log.info("User in DB {}",user);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
            }

            if (credentialsDTO.getUserName() != null && !credentialsDTO.getUserName().isBlank()) {
                String newUserName = credentialsDTO.getUserName();

                if (!newUserName.equals(currentUsername)) {
                    User existingUser = userService.findByUserName(newUserName);
                    if (existingUser != null) {
                        return new ResponseEntity<>("User Name Already Exists", HttpStatus.CONFLICT);
                    }
                    user.setUserName(newUserName);
                    userService.saveUser(user);
                }
            }

            if (credentialsDTO.getPassword() != null && !credentialsDTO.getPassword().isBlank()) {
                userService.changePass(user, credentialsDTO.getPassword());
                userService.saveUser(user);
            }

            return ResponseEntity.ok("Credentials Updated");

        }catch (Exception e){
            return new ResponseEntity<>("Error Updating Credentials" + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * Delete the currently authenticated user
     */
    @Transactional
    @DeleteMapping
    public ResponseEntity<?> deleteUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            User user = userService.findByUserName(userName);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            List<JournalEntry> journalEntryList = user.getJournalEntryList();

            if (journalEntryList != null && !journalEntryList.isEmpty()) {
                journalEntryRepository.deleteAll(journalEntryList);
            }

            userRepository.deleteByUserName(userName);

            return new ResponseEntity<>("User and their Journal entries deleted Successfully ", HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("Error deleting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get weather details for a specified city.
     * Example: /users/mumbai OR /users/new york
     */
    @GetMapping("/{city}")
    public ResponseEntity<?> getWeatherDetails(@PathVariable String city){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            String formattedCity = weatherService.formatCityName(city);

            WeatherResponse resp = weatherService.getWeather(formattedCity);

            String greet = (resp != null) ? " The weather in "+ formattedCity + " feels like " + resp.getCurrent().getFeelslike() : "";

            return ResponseEntity.ok("Hi, " + userName + greet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid city name: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching weather details: " + e.getMessage());
        }
    }


}
