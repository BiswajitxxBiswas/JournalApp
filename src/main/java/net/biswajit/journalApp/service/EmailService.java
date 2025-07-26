package net.biswajit.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body){
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);

            javaMailSender.send(simpleMailMessage);
        }catch (Exception e){
            log.error("Exception while sending mail",e);
        }
    }

    public void sendOtpEmail(String email, String userName, String otp, int validMinutes) {
        String subject = "Your JournalApp OTP";
        String body = "Hello " + userName + ",\n\n"
                + "Your email verification OTP: " + otp
                + "\nThis OTP expires in " + validMinutes + " minutes.";
        // Prepare email (add content type, HTML support as needed)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

}
