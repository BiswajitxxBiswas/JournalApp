package net.biswajit.journalApp.service;

import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;
    @Autowired
    private UserService userService;

    @Transactional
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

    @Transactional
    public boolean deleteEntryById(ObjectId id, String userName){
        boolean result = false;
        try {
            User user = userService.findByUserName(userName);
            result = user.getJournalEntryList().removeIf(x -> x.getId().equals(id));
            if(result){
                userService.saveUser(user);
                journalEntryRepository.deleteById(id);
                return true;
            }
        }catch (Exception e){
            throw new RuntimeException("An error occurred while deleting the entry "+e);
        }
        return false;
    }
}
