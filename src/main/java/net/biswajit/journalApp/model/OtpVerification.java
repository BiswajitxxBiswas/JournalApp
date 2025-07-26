package net.biswajit.journalApp.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "otp_verification")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class OtpVerification {
    @Id
    private String id;

    private String email;
    private String otp;
    private LocalDateTime expiryTime;
}
