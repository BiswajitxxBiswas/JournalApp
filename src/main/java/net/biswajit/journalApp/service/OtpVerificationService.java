package net.biswajit.journalApp.service;

import net.biswajit.journalApp.model.EmailOtp;
import net.biswajit.journalApp.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpVerificationService {

    @Autowired
    private OtpVerificationRepository otpRepo;

    @Autowired
    private EmailService emailService;

    private static final int EXPIRY_MINUTES = 10;

    public String generateOtp() {
        return String.valueOf(100_000 + new SecureRandom().nextInt(900_000));
    }


    @Transactional
    public void sendOtp(String email, String userName) {
        String otp = generateOtp();

        otpRepo.deleteByEmail(email);

        EmailOtp emailOtp = EmailOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .build();

        otpRepo.save(emailOtp);

        emailService.sendOtpEmail(email, userName, otp, EXPIRY_MINUTES);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        var emailOtpOpt = otpRepo.findByEmail(email);
        if (emailOtpOpt.isEmpty()) return false;

        EmailOtp emailOtp = emailOtpOpt.get();

        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepo.deleteByEmail(email);
            return false;
        }

        if (!emailOtp.getOtp().equals(otp)) {
            return false;
        }

        otpRepo.deleteByEmail(email);
        return true;
    }
}
