package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TraineeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);
    private final AtomicLong idCounter = new AtomicLong(1);

    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    public Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Trainee trainee = new Trainee();
        trainee.setId(idCounter.getAndIncrement());
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setActive(true);

        String username = generateUsername(firstName, lastName);
        String password = generateRandomPassword();
        trainee.setUsername(username);
        trainee.setPassword(password);

        traineeDAO.save(trainee);
        LOGGER.info("Successfully created trainee with username: {}", username);
        return trainee;
    }

    public Optional<Trainee> selectTraineeProfile(Long id) {
        LOGGER.info("Attempting to select trainee with ID: {}", id);
        return traineeDAO.findById(id);
    }

    public Trainee updateTraineeProfile(Trainee trainee) {
        LOGGER.info("Updating trainee profile for user: {}", trainee.getUsername());
        return traineeDAO.save(trainee);
    }

    public void deleteTraineeProfile(Long id) {
        LOGGER.info("Deleting trainee profile with ID: {}", id);
        traineeDAO.deleteById(id);
    }

    private String generateUsername(String firstName, String lastName) {
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
        boolean takenByTrainee = traineeDAO.findAll().stream()
                .anyMatch(t -> t.getUsername().equalsIgnoreCase(username));
        boolean takenByTrainer = trainerDAO.findAll().stream()
                .anyMatch(t -> t.getUsername().equalsIgnoreCase(username));
        return takenByTrainee || takenByTrainer;
    }

    private String generateRandomPassword() {
        final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return password.toString();
    }
}