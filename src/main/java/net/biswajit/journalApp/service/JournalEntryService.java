package net.biswajit.journalApp.service;

import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;
    @Autowired
    private UserService userService;

    public void saveEntry(JournalEntry newEntry, String userName){
        User user = userService.findByUserName(userName);
        newEntry.setDate(LocalDate.now());
        JournalEntry saved = journalEntryRepository.save(newEntry);
        user.getJournalEntryList().add(saved);
        userService.saveUser(user);

    }
    public void saveEntry(JournalEntry newEntry){
        newEntry.setDate(LocalDate.now());
        JournalEntry saved = journalEntryRepository.save(newEntry);
    }

    public List<JournalEntry> getAllEntry(){
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> getEntryById(ObjectId id){
        return journalEntryRepository.findById(id);
    }

    public void deleteEntryById(ObjectId id, String userName){
        User user = userService.findByUserName(userName);
        journalEntryRepository.deleteById(id);
        user.getJournalEntryList().removeIf(x -> x.getId().equals(id));
        userService.saveUser(user);
    }
}
