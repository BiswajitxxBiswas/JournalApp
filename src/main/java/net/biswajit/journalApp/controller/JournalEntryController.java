package net.biswajit.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.dto.JournalEntryDTO;
import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.service.JournalEntryService;
import net.biswajit.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/journal")
@Slf4j
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;
    @Autowired
    private UserService userService;



    /**
     * Retrieves all journal entries of the authenticated user.
     */
    @GetMapping
    public ResponseEntity<?> getAllEntry() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        User user = userService.findByUserName(userName);
        List<JournalEntry> journalEntries = user.getJournalEntryList();

        if (journalEntries != null && !journalEntries.isEmpty()) {
            return new ResponseEntity<>(journalEntries, HttpStatus.OK);

        }
        return new ResponseEntity<>("No journal entries found ", HttpStatus.NOT_FOUND);
    }

    /**
     * Retrieves a specific journal entry by its ID.
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getEntryById(@PathVariable String id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            ObjectId myId = new ObjectId(id);
            User user = userService.findByUserName(userName);
            List<JournalEntry> collect = user.getJournalEntryList().stream()
                    .filter(x -> x.getId().equals(myId))
                    .toList();

            if (!collect.isEmpty()) {
                Optional<JournalEntry> journalEntry = journalEntryService.getEntryById(myId);
                if (journalEntry.isPresent()) {
                    return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
                }
            }
            return new ResponseEntity<>("Journal entry not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid journal entry ID format", HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Creates a new journal entry for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<?> postEntry(@RequestBody JournalEntryDTO journalEntryDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {

            JournalEntry journalEntry = journalEntryService.convertToEntity(journalEntryDTO);
            journalEntryService.saveEntry(journalEntry, userName);

            return new ResponseEntity<>("Journal entry created successfully \n" + journalEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create journal entry", HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Updates an existing journal entry.
     */
    @PutMapping("id/{id}")
    public ResponseEntity<?> updateEntry(
            @PathVariable String id,
            @RequestBody JournalEntryDTO journalEntryDTO
    ) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            ObjectId myId = new ObjectId(id);
            JournalEntry journalEntry = journalEntryService.convertToEntity(journalEntryDTO);
            User user = userService.findByUserName(userName);

            Optional<JournalEntry> optionalEntry = journalEntryService.getEntryById(myId);

            if (optionalEntry.isPresent() && user.getJournalEntryList().contains(optionalEntry.get())) {
                JournalEntry oldEntry = optionalEntry.get();

                if (journalEntry.getTitle() != null && !journalEntry.getTitle().isEmpty()) {
                    oldEntry.setTitle(journalEntry.getTitle());
                }
                if (journalEntry.getContent() != null && !journalEntry.getContent().isEmpty()) {
                    oldEntry.setContent(journalEntry.getContent());
                }
                if (journalEntry.getSentiments() != null) {
                    oldEntry.setSentiments(journalEntry.getSentiments());
                }

                journalEntryService.saveEntry(oldEntry);
                return new ResponseEntity<>("Journal entry updated successfully \n" + oldEntry, HttpStatus.OK);
            }
            return new ResponseEntity<>("Journal entry not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid journal entry ID format", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes a journal entry by ID.
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable String id) {

        ObjectId myId = new ObjectId(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            boolean result = journalEntryService.deleteEntryById(myId, userName);
            if (result) {
                return new ResponseEntity<>("Journal entry deleted successfully ", HttpStatus.OK);
            }
            return new ResponseEntity<>("Journal entry not found ", HttpStatus.NOT_FOUND);
        }catch (Exception e){
            return new ResponseEntity<>("Invalid journal entry ID format ", HttpStatus.BAD_REQUEST);
        }
    }

}
