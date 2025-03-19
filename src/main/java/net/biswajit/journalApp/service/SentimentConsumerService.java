package net.biswajit.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.model.SentimentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SentimentConsumerService {

    @Autowired
    private EmailService emailService;


    @KafkaListener(
            topics = "weekly-sentiments",
            groupId = "weekly-sentiment-group"
    )
    public void consume(SentimentData sentimentData) {
        log.info("Received message: {}", sentimentData);
        try {
            sendEmail(sentimentData);
        } catch (Exception e) {
            log.error("Error processing message: ", e);
        }
    }

    private void sendEmail(SentimentData sentimentData) {
        emailService.sendEmail(sentimentData.getEmail(), "Sentiment for previous week", sentimentData.getSentiment());
    }
}