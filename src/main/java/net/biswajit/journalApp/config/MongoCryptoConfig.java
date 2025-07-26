package net.biswajit.journalApp.config;

import net.biswajit.journalApp.entity.JournalEntry;
import net.biswajit.journalApp.enums.Sentiments;
import net.biswajit.journalApp.utils.CryptoUtil;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Configuration
public class MongoCryptoConfig {

    @Bean
    public MongoCustomConversions customConversions(CryptoUtil cryptoUtil) {
        return new MongoCustomConversions(List.of(
                new JournalWriteConverter(cryptoUtil),
                new JournalReadConverter(cryptoUtil)
        ));
    }

    static class JournalWriteConverter implements Converter<JournalEntry, Document> {
        private final CryptoUtil crypto;
        JournalWriteConverter(CryptoUtil crypto) { this.crypto = crypto; }

        @Override
        public Document convert(JournalEntry src) {
            Document doc = new Document();
            if (src.getId() != null) doc.put("_id", src.getId());
            doc.put("date", src.getDate());
            doc.put("tags", src.getTags());
            doc.put("sentiments", src.getSentiments());
            doc.put("titleCipher", crypto.encrypt(src.getTitle()));
            doc.put("contentCipher", crypto.encrypt(src.getContent()));
            return doc;
        }
    }

    static class JournalReadConverter implements Converter<Document, JournalEntry> {
        private final CryptoUtil crypto;
        JournalReadConverter(CryptoUtil crypto) { this.crypto = crypto; }

        @Override
        public JournalEntry convert(Document d) {
            JournalEntry j = new JournalEntry();
            j.setId(d.getObjectId("_id"));
            j.setDate(d.getDate("date").toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            j.setTags((List<String>) d.get("tags"));
            j.setSentiments(Sentiments.valueOf(d.getString("sentiments")));
            j.setTitle(crypto.decrypt(d.getString("titleCipher")));
            j.setContent(crypto.decrypt(d.getString("contentCipher")));
            return j;
        }
    }
}
