package net.biswajit.journalApp.service;

import net.biswajit.journalApp.model.OtpVerification;
import net.biswajit.journalApp.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpVerificationService {

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private EmailService emailService;

    public void generateAndSendOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        otpVerificationRepository.save(new OtpVerification(null, email, otp, expiryTime));

        String subject = "Verify your Email - Journal App";
        String body = "Your OTP for Journal App verification is: " + otp + "\nIt will expire in 5 minutes.";

        emailService.sendEmail(email, subject, body);
    }
}
