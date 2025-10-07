package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.createTraineeProfile(firstName, lastName, dateOfBirth, address);
    }

    public Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        return trainerService.createTrainerProfile(firstName, lastName, specialization);
    }

    public Optional<Trainee> getTraineeProfile(String username, String password) {
        if (!traineeService.checkTraineeCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", username);
            return Optional.empty();
        }
        return traineeService.selectTraineeProfileByUsername(username);
    }

    public Optional<Trainee> updateTraineeProfile(String username, String password, String firstName, String lastName, LocalDate dob, String address, boolean isActive) {
        if (!traineeService.checkTraineeCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", username);
            return Optional.empty();
        }
        return traineeService.updateTraineeProfile(username, firstName, lastName, dob, address, isActive);
    }
    
    public boolean deleteTraineeProfile(String username, String password) {
        if (!traineeService.checkTraineeCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", username);
            return false;
        }
        return traineeService.deleteTraineeProfileByUsername(username);
    }

    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        return traineeService.changeTraineePassword(username, oldPassword, newPassword);
    }

    public boolean activateDeactivateTrainee(String username, String password, boolean isActive) {
        if (!traineeService.checkTraineeCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", username);
            return false;
        }
        return traineeService.activateDeactivateTrainee(username, isActive);
    }
    
    public Optional<Trainer> getTrainerProfile(String username, String password) {
        if (!trainerService.checkTrainerCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainer: {}", username);
            return Optional.empty();
        }
        return trainerService.selectTrainerProfileByUsername(username);
    }

    public Optional<Trainer> updateTrainerProfile(String username, String password, String firstName, String lastName, TrainingType specialization, boolean isActive) {
        if (!trainerService.checkTrainerCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainer: {}", username);
            return Optional.empty();
        }
        return trainerService.updateTrainerProfile(username, firstName, lastName, specialization, isActive);
    }

    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        return trainerService.changeTrainerPassword(username, oldPassword, newPassword);
    }
    
    public boolean activateDeactivateTrainer(String username, String password, boolean isActive) {
        if (!trainerService.checkTrainerCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainer: {}", username);
            return false;
        }
        return trainerService.activateDeactivateTrainer(username, isActive);
    }

    public Training addTraining(String traineeUsername, String traineePassword, String trainerUsername, String trainingName, TrainingType trainingType, LocalDate date, int duration) {
        if (!traineeService.checkTraineeCredentials(traineeUsername, traineePassword)) {
            LOGGER.warn("Authentication failed for trainee: {}", traineeUsername);
            return null;
        }
        return trainingService.addTraining(traineeUsername, trainerUsername, trainingName, trainingType, date, duration);
    }

    public List<Object[]> getTraineeTrainingsList(String username, String password, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingType) {
        if (!traineeService.checkTraineeCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", username);
            return Collections.emptyList();
        }
        return traineeService.getTraineeTrainingsList(username, fromDate, toDate, trainerName, trainingType);
    }

    public List<Object[]> getTrainerTrainingsList(String username, String password, LocalDate fromDate, LocalDate toDate, String traineeName) {
        if (!trainerService.checkTrainerCredentials(username, password)) {
            LOGGER.warn("Authentication failed for trainer: {}", username);
            return Collections.emptyList();
        }
        return trainerService.getTrainerTrainingsList(username, fromDate, toDate, traineeName);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername, String password) {
        if (!traineeService.checkTraineeCredentials(traineeUsername, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", traineeUsername);
            return Collections.emptyList();
        }
        return traineeService.getUnassignedTrainersForTrainee(traineeUsername);
    }

    public Optional<List<Trainer>> updateTraineeTrainers(String traineeUsername, String password, List<String> trainerUsernames) {
        if (!traineeService.checkTraineeCredentials(traineeUsername, password)) {
            LOGGER.warn("Authentication failed for trainee: {}", traineeUsername);
            return Optional.empty();
        }
        return traineeService.updateTraineeTrainersList(traineeUsername, trainerUsernames);
    }
}