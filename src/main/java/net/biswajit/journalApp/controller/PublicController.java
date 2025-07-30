package net.biswajit.journalApp.controller;


import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.dto.UserDTO;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.model.EmailOtp;
import net.biswajit.journalApp.repository.OtpVerificationRepository;
import net.biswajit.journalApp.service.OtpVerificationService;
import net.biswajit.journalApp.service.RedisService;
import net.biswajit.journalApp.service.UserDetailsServiceImpl;
import net.biswajit.journalApp.service.UserService;
import net.biswajit.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

    @Autowired
    private OtpVerificationService otpService;

    @Autowired
    private OtpVerificationRepository otpRepo;

    @Autowired
    private RedisService redisService;


    /**
     * Registers a new user.
     *
     */
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try{

            EmailOtp mail = otpRepo.findByEmail(userDTO.getEmail()).orElse(null);
            if(mail != null){
                return new ResponseEntity<>("Email Exist",HttpStatus.BAD_REQUEST);
            }

            User user = userService.convertToEntity(userDTO);
            user.setMailVerify(false);
            userService.saveNewUser(user);

            otpService.sendOtp(user.getEmail(),user.getUserName());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    "Registered. OTP sent to your email for verification.");
        }catch(Exception e){
            log.error("Error occurred while registering user {} ",e.getMessage(),e);
            return new ResponseEntity<>("Error registering user ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        User user = userService.findByEmail(email);
        log.info("User data {}",user);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        if (user.isMailVerify())
            return ResponseEntity.ok("Email already verified! You can log in.");

        boolean valid = otpService.verifyOtp(email, otp);
        if (!valid)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");

        user.setMailVerify(true);
        userService.saveUser(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());

        String accessToken = jwtUtil.generateToken(userDetails.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

        redisService.set(
                "refresh_token:"+ refreshToken,
                user.getId(),
                7 * 24 * 60 * 60L
        );

        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(2 * 60 * 60L)      // 2 hours in seconds
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60L) // 7 days in seconds
                .sameSite("Strict")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return  ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("user", user));

    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        clearOtp(email);
        otpService.sendOtp(user.getEmail(), user.getUserName());
        return ResponseEntity.ok("OTP resent!");
    }

    public void clearOtp(String email){
        otpRepo.deleteByEmail(email);
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

            User user = userService.findByUserName(userDTO.getUserName());
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUserName());

            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            redisService.set(
                    "refresh_token:"+ refreshToken,
                    user.getId(),
                    7 * 24 * 60 * 60L
            );

            ResponseCookie accessCookie = ResponseCookie
                    .from("access_token", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(5 * 60 * 60L)      // 5 hours in seconds
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie
                    .from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60L) // 7 days in seconds
                    .sameSite("Strict")
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            return  ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of("user", user));

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
