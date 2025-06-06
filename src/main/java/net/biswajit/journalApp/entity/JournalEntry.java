package net.biswajit.journalApp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.biswajit.journalApp.enums.Sentiments;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "journal_entries")
@Data
@NoArgsConstructor
public class JournalEntry {

    @Id
    private ObjectId id;
    private String title;
    private String content;
    private LocalDate date;
    private Sentiments sentiments;

    @JsonProperty("id")
    public String getIdAsString() {
        return id.toHexString();
    }


}
