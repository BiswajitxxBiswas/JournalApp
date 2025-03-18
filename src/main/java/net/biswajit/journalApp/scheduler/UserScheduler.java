package net.biswajit.journalApp.scheduler;

import net.biswajit.journalApp.cache.AppCache;
import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.enums.Sentiments;
import net.biswajit.journalApp.model.SentimentData;
import net.biswajit.journalApp.repository.UserRepositoryImpl;
import net.biswajit.journalApp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler {
    @Autowired
    private AppCache appCache;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KafkaTemplate<String, SentimentData> kafkaTemplate;


    @Scheduled(cron = "0 0 9 * * SUN") //Every Sunday 9 AM CRON
    public void fetchUserAndSendSaMail(){
        List<User> userlist = userRepository.allUserSA();
        for(User users : userlist){
            List<JournalEntry> entries = users.getJournalEntryList();
            List<Sentiments> filteredSentiments = entries.stream().filter(x->x.getDate().isAfter(LocalDate.now().minus(7, ChronoUnit.DAYS))).map(x->x.getSentiments()).collect(Collectors.toList());
            HashMap<Sentiments,Integer> sentimentCount = new HashMap<>();
            for(Sentiments sentiments : filteredSentiments){
                if(sentiments != null){
                    sentimentCount.put(sentiments,sentimentCount.getOrDefault(sentiments,0)+1);
                }
            }
            Sentiments mostFreqSentiments = null;
            int maxCount = 0;
            for(Map.Entry<Sentiments,Integer> entry : sentimentCount.entrySet()){
                if(entry.getValue() > maxCount){
                    maxCount = entry.getValue();
                    mostFreqSentiments = entry.getKey();
                }
            }
            if(mostFreqSentiments != null){
                SentimentData sentimentData = SentimentData.builder()
                        .email(users.getEmail())
                        .sentiment("Sentiment for the last 7 days "+mostFreqSentiments)
                        .build();

                kafkaTemplate.send("weekly-sentiments", sentimentData.getEmail(), sentimentData);
            }
        }
    }

    @Scheduled(cron = "0 0/10 * ? * *") // Every 10 Min CRON
    public void clearCache(){
        appCache.init();
    }
}
