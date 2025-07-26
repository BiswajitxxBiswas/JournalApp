package net.biswajit.journalApp.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "otp_verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EmailOtp {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String otp;
    private LocalDateTime expiryTime;
}
