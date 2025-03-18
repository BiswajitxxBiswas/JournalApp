package net.biswajit.journalApp.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTests {

    @Autowired
    private EmailService emailService;

    @Test
    @Disabled
    public void test(){
        emailService.sendEmail(
                "bbiswajit1999sh@gmail.com",
                "Greetings",
                "Hello, Brother how r u ??"
        );
    }
}
