package net.biswajit.journalApp.service;


import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.dto.UserDTO;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveUser(User user){
        userRepository.save(user);
    }

    public void saveNewUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of("USER"));
        userRepository.save(user);
    }

    public void saveAdmin(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER","ADMIN"));
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
            log.error("Error fetching user: ",e);
            return null;
        }
    }

    public User updateUserFromDTO(User user, UserDTO userDTO) throws Exception {
        try {
            if (userDTO == null) {
                throw new IllegalArgumentException("UserDTO cannot be null");
            }

            if (userDTO.getUserName() != null && !userDTO.getUserName().isEmpty()) {
                user.setUserName(userDTO.getUserName());
            }

            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPassword(userDTO.getPassword());
            }

            if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
                user.setEmail(userDTO.getEmail());
            }

            if (userDTO.isSentimentAnalysis() && (user.getEmail() == null || user.getEmail().isEmpty()) ) {
                throw new IllegalArgumentException("Email is required for sentiment analysis.");
            }

            if (!userDTO.isSentimentAnalysis()) {
                user.setSentimentAnalysis(false);
            }

            return user;

        } catch (Exception e) {
            log.error("Error updating user from DTO: {}", e.getMessage(), e);
            return null;
        }
    }


    public User convertToEntity(UserDTO userDTO) {
        try {
            User user = new User();
            user.setUserName(userDTO.getUserName());
            user.setPassword(userDTO.getPassword());
            user.setEmail(userDTO.getEmail());
            user.setSentimentAnalysis(userDTO.isSentimentAnalysis());
            return user;
        } catch (Exception e) {
            log.error("Error occurred while converting UserDTO to User: {}", e.getMessage(), e);
            return null;
        }
    }


}
