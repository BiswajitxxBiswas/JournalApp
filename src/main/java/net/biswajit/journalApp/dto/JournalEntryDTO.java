package net.biswajit.journalApp.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.biswajit.journalApp.enums.Sentiments;
import org.bson.types.ObjectId;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntryDTO {

    private ObjectId id;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDate date;
    private Sentiments sentiments;

}
