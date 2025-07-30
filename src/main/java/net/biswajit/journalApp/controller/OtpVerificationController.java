package net.biswajit.journalApp.controller;

import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.model.EmailOtp;
import net.biswajit.journalApp.repository.OtpVerificationRepository;
import net.biswajit.journalApp.repository.UserRepository;
import net.biswajit.journalApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class OtpVerificationController {

    @Autowired
    private OtpVerificationRepository otpRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserService userService;

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        EmailOtp mail = otpRepo.findByEmail(email)
                .orElse(null);

        if (mail == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP not found for this email");
        }


        if (mail.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP has expired");
        }

        if (!mail.getOtp().equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }


        User user = userRepo.findByEmail(email).orElse(null);
        if(user != null){
            user.setMailVerify(true);
            userService.saveUser(user);
        }

        otpRepo.delete(mail);

        return ResponseEntity.ok("Email verified successfully");
    }
}
