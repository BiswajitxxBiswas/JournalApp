package net.biswajit.journalApp.service;


import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveUser(User user){
        userRepository.save(user);
    }

    public void saveNewUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER"));
        userRepository.save(user);
    }

    public List<User> getAllUser(){
        return new ArrayList<>(userRepository.findAll());
    }

    public Optional<User> getUserById(ObjectId id){
        return userRepository.findById(id);
    }

    public void deleteUserById(ObjectId id){
        userRepository.deleteById(id);
    }

    public User findByUserName(String username) {
        try {
            return userRepository.findByUserName(username);
        } catch (Exception e) {
            System.err.println("Error fetching user: " + e.getMessage());
            return null;
        }
    }

}
