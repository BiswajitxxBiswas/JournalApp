package net.biswajit.journalApp.repository;

import net.biswajit.journalApp.entity.ConfigJournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigJournalRepository extends MongoRepository<ConfigJournalEntry, ObjectId> {
}
