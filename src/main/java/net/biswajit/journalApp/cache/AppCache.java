package net.biswajit.journalApp.cache;

import net.biswajit.journalApp.entity.ConfigJournalEntry;
import net.biswajit.journalApp.repository.ConfigJournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AppCache {

    public enum keys{
        WEATHER_API;
    }

    @Autowired
    private ConfigJournalRepository configJournalAppRepository;

    public Map<String,String> appCache;


    @PostConstruct
    public void init(){
        appCache = new HashMap<>();
        List<ConfigJournalEntry> list = configJournalAppRepository.findAll();
        for(ConfigJournalEntry values : list){
            appCache.put(values.getKey(),values.getValue());
        }


    }
}
