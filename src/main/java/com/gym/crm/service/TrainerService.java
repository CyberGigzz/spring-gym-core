package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);
    private final AtomicLong idCounter = new AtomicLong(1);

    private TrainerDAO trainerDAO;
    private TraineeDAO traineeDAO; 

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    public Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        Trainer trainer = new Trainer();
        trainer.setId(idCounter.getAndIncrement());
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        trainer.setActive(true);

        String username = generateUsername(firstName, lastName);
        String password = generateRandomPassword();
        trainer.setUsername(username);
        trainer.setPassword(password);

        trainerDAO.save(trainer);
        LOGGER.info("Successfully created trainer with username: {}", username);
        return trainer;
    }

    public Optional<Trainer> selectTrainerProfile(Long id) {
        LOGGER.info("Attempting to select trainer with ID: {}", id);
        return trainerDAO.findById(id);
    }

    public Trainer updateTrainerProfile(Trainer trainer) {
        LOGGER.info("Updating trainer profile for user: {}", trainer.getUsername());
        return trainerDAO.save(trainer);
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