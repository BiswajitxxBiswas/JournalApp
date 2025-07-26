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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
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
    @GetMapping("/{id}")
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
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateEntry(@PathVariable String id,
                                         @RequestBody JournalEntryDTO journalEntryDTO) {
        try {
            ObjectId entryId = new ObjectId(id);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            User user = userService.findByUserName(userName);
            Optional<JournalEntry> optionalEntry = journalEntryService.getEntryById(entryId);

            if (optionalEntry.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Journal entry not found");
            }

            JournalEntry entry = optionalEntry.get();
            boolean isOwner = user.getJournalEntryList().contains(entry);

            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Unauthorized to update this journal entry");
            }

            if (journalEntryDTO.getTitle() != null && !journalEntryDTO.getTitle().isBlank()) {
                entry.setTitle(journalEntryDTO.getTitle());
            }
            if (journalEntryDTO.getContent() != null && !journalEntryDTO.getContent().isBlank()) {
                entry.setContent(journalEntryDTO.getContent());
            }
            if (journalEntryDTO.getSentiments() != null) {
                entry.setSentiments(journalEntryDTO.getSentiments());
            }
            if(journalEntryDTO.getTags() != null && !journalEntryDTO.getTags().isEmpty()){
                entry.setTags(journalEntryDTO.getTags());
            }

            journalEntryService.updateEntry(entry);
            return ResponseEntity.ok("Journal entry updated successfully\n" + entry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid journal entry ID format");
        }
    }



    /**
     * Deletes a journal entry by ID.
     */
    @DeleteMapping("/{id}")
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
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid journal entry ID format ", HttpStatus.BAD_REQUEST);
        }
    }

}
