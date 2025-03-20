package net.biswajit.journalApp.controller;


import lombok.extern.slf4j.Slf4j;
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

    /**
     * Update user details
     */
    @PutMapping("/update-user")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            User userInDB = userService.findByUserName(userName);

            if(userInDB != null){

                User updatedUser = userService.updateUserFromDTO(userInDB, userDTO);

                if (updatedUser == null) {
                    return new ResponseEntity<>("Error updating user details", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                userService.saveNewUser(updatedUser);
                return ResponseEntity.ok("User updated successfully");
            }
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
