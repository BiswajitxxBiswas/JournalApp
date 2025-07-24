package net.biswajit.journalApp.controller;


import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.dto.UserDTO;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.service.UserDetailsServiceImpl;
import net.biswajit.journalApp.service.UserService;
import net.biswajit.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


/**
 * Public API controller for handling user authentication and registration.
 */
@RestController
@CrossOrigin
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registers a new user.
     *
     */
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try{
            User user = userService.convertToEntity(userDTO);
            userService.saveNewUser(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            Map<String,Object> response = new HashMap<>();
            response.put("token",jwt);
            response.put("user",Map.of(
                    "userName", user.getUserName(),
                    "email", user.getEmail()
            ));
            log.info("User data {}",response);

            return  ResponseEntity.ok(response);
        }catch(Exception e){
            log.error("Error occurred while registering user {} ",e.getMessage(),e);
            return new ResponseEntity<>("Error registering user ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDTO.getUserName(),
                            userDTO.getPassword()
                    )
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return  ResponseEntity.ok(Map.of("token",jwt));
        } catch (Exception e) {
            log.error("Exception occurred while creating JWT Token: {}", e.getMessage(), e);
            return new ResponseEntity<>("Incorrect Username or Password ", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Performs a health check to verify if the API is running.
     *
     */
    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        try {
            return ResponseEntity.ok("Ok");
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage(), e);
            return new ResponseEntity<>("Health check failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
