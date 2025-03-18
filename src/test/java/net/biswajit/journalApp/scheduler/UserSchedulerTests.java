package net.biswajit.journalApp.scheduler;

import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.enums.Sentiments;
import net.biswajit.journalApp.model.SentimentData;
import net.biswajit.journalApp.repository.UserRepository;
import net.biswajit.journalApp.repository.UserRepositoryImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class UserSchedulerTests {

    @Autowired
    private UserScheduler userScheduler;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
//    @Disabled
    public void saMailScheduler(){
        userScheduler.fetchUserAndSendSaMail();
    }


}
