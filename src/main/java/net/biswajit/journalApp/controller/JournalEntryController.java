package net.biswajit.journalApp.controller;

import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.service.JournalEntryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/entries")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @GetMapping
    public List<JournalEntry> getAllEntry(){
        return journalEntryService.getAllEntry();
    }

    @GetMapping("/id/{myId}")
    public JournalEntry getEntryById(@PathVariable ObjectId myId){
        return journalEntryService.getEntryById(myId).orElse(null);
    }

    @PostMapping
    public JournalEntry postEntry(@RequestBody JournalEntry myEntry){
        myEntry.setDate(LocalDate.now());
        journalEntryService.saveEntry(myEntry);
        return myEntry;
    }

    @PutMapping("id/{myId}")
    public boolean updateEntry(@PathVariable ObjectId myId, @RequestBody JournalEntry newEntry){
        JournalEntry oldEntry = getEntryById(myId);
        if(oldEntry != null){
            oldEntry.setTitle(newEntry.getTitle() != null && !newEntry.getTitle().isEmpty() ? newEntry.getTitle() : oldEntry.getTitle());
            oldEntry.setContent(newEntry.getContent() != null && !newEntry.getContent().isEmpty() ? newEntry.getContent() : oldEntry.getContent());
        }
        journalEntryService.saveEntry(oldEntry);
        return true;
    }

    @DeleteMapping("/id/{myId}")
    public boolean deleteEntry(@PathVariable ObjectId myId){
        journalEntryService.deleteEntryById(myId);
        return true;
    }

}
