package net.biswajit.journalApp.repository;

import net.biswajit.journalApp.model.EmailOtp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends MongoRepository<EmailOtp,String> {
    Optional<EmailOtp> findByEmail(String email);
    void deleteByEmail(String email);
}
