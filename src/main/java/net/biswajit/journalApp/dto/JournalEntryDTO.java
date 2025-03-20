package net.biswajit.journalApp.dto;

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
    private LocalDate date;
    private Sentiments sentiments;

}
