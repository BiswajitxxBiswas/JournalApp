package net.biswajit.journalApp.repository;

import net.biswajit.journalApp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<User> allUserSA() {
        Query query = new Query();

        Criteria criteria = new Criteria();
        query.addCriteria(criteria.andOperator(
                Criteria.where("email").regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"),
                Criteria.where("sentimentAnalysis").is(true),
                Criteria.where("roles").in("USER","ADMIN"))
        );

        List<User> userList = mongoTemplate.find(query, User.class);
        return userList;
    }
}
