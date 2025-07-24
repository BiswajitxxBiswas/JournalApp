package net.biswajit.journalApp.service;

import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.dto.JournalEntryDTO;
import net.biswajit.journalApp.dto.UserDTO;
import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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
        journalEntryRepository.save(newEntry);
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
            log.error("Error occurred while deleting entry {} ",e.getMessage(),e);
        }
        return false;
    }

    public JournalEntry convertToEntity(JournalEntryDTO journalEntryDTO) {
        try {
            JournalEntry journalEntry = new JournalEntry();

            journalEntry.setTitle(journalEntryDTO.getTitle());
            journalEntry.setContent(journalEntryDTO.getContent());
            journalEntry.setTags(journalEntryDTO.getTags());
            journalEntry.setSentiments(journalEntryDTO.getSentiments());

            return journalEntry;
        } catch (Exception e) {
            log.error("Error occurred while converting JournalEntryDTO to JournalEntry: {}", e.getMessage(), e);
            return null;
        }
    }
}
