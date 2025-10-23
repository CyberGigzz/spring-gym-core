package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; // Import the DTO
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UserService {

    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final PasswordEncoder passwordEncoder;

    public UserService(TraineeDAO traineeDAO, TrainerDAO trainerDAO, PasswordEncoder passwordEncoder) {
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName.toLowerCase() + "." + lastName.toLowerCase();
        String finalUsername = baseUsername;
        int serial = 1;

        while (isUsernameTaken(finalUsername)) {
            finalUsername = baseUsername + serial;
            serial++;
        }
        return finalUsername;
    }

    private boolean isUsernameTaken(String username) {
        return traineeDAO.findByUsername(username).isPresent() || trainerDAO.findByUsername(username).isPresent();
    }

    /**
     * Generates a random plain text password.
     * @return The plain text password.
     */
    public String generatePlainPassword() {
        final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return password.toString();
    }

    /**
     * Encodes a plain text password using the configured encoder.
     * @param plainPassword The plain text password.
     * @return The encoded password hash.
     */
    public String encodePassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
}