package net.biswajit.journalApp.controller;

import net.biswajit.journalApp.entity.JournalEntry;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/entries")
public class JournalEntryController {

    private Map<Long, JournalEntry> journalEntries = new HashMap<>();

    @GetMapping
    public List<JournalEntry> getAllEntry(){
        return new ArrayList<>(journalEntries.values());
    }

    @GetMapping("/id/{myId}")
    public JournalEntry getEntryById(@PathVariable long myId){
        return journalEntries.get(myId);
    }

    @PostMapping
    public boolean postEntry(@RequestBody JournalEntry myEntry){
        journalEntries.put(myEntry.getId(),myEntry);
        return true;
    }

    @PutMapping("id/{myId}")
    public boolean updateEntry(@PathVariable long myId, @RequestBody JournalEntry myEntry){
        journalEntries.put(myId,myEntry);
        return true;
    }

    @DeleteMapping("/id/{myId}")
    public JournalEntry deleteEntry(@PathVariable long myId){
        return journalEntries.remove(myId);
    }

}
