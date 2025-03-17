package net.biswajit.journalApp.scheduler;

import net.biswajit.journalApp.cache.AppCache;
import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.UserRepositoryImpl;
import net.biswajit.journalApp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserScheduler {
    @Autowired
    private AppCache appCache;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private EmailService emailService;

//    @Scheduled(cron = "0 0 9 * * SUN")
    @Scheduled(cron = "0 * * * * *")
    public void fetchUserAndSendSaMail(){
        List<User> userlist = userRepository.allUserSA();
        for(User users : userlist){
            List<JournalEntry> entries = users.getJournalEntryList();
            List<String> filteredEntries = entries.stream().filter(x->x.getDate().isAfter(LocalDate.now().minus(7, ChronoUnit.DAYS))).map(x->x.getContent()).collect(Collectors.toList());
            String entry = String.join(" ",filteredEntries);
            emailService.sendEmail(users.getEmail(),"Sentiments for Last 7 Days",entry);

        }
    }

    @Scheduled(cron = "0 0/10 * ? * *")
    public void clearCache(){
        appCache.init();
    }
}
