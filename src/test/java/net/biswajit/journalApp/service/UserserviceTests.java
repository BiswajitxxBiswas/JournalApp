package net.biswajit.journalApp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserserviceTests {

    @Autowired
    private UserService userService;

//    @Test
    @ParameterizedTest
//    @CsvSource({
//            "Ram",
//            "biswajit1999"
//    })
    @ValueSource(strings = {
            "Ram",
//            "biswajit1999"
    })
    public void testfindByUserName(String name){
        assertNotNull(userService.findByUserName(name));
    }
}
