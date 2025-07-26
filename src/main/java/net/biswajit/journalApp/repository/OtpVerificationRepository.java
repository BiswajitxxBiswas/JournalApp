package net.biswajit.journalApp.repository;

import net.biswajit.journalApp.model.OtpVerification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends MongoRepository<OtpVerification,String> {
    Optional<OtpVerification> findByEmail(String email);
}
