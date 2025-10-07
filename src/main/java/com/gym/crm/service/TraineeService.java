package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TraineeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);
    private final UserService userService; 
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TraineeService(UserService userService, TraineeDAO traineeDAO, TrainerDAO trainerDAO) {
        this.userService = userService;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
    }

    public Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setActive(true);

        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generateRandomPassword();
        trainee.setUsername(username);
        trainee.setPassword(password);

        traineeDAO.save(trainee);
        LOGGER.info("Successfully created trainee with username: {}", username);
        return trainee;
    }

    public boolean checkTraineeCredentials(String username, String password) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        return traineeOpt.map(trainee -> trainee.getPassword().equals(password)).orElse(false);
    }

    public Optional<Trainee> selectTraineeProfileByUsername(String username) {
        return traineeDAO.findByUsername(username);
    }

    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        if (!checkTraineeCredentials(username, oldPassword)) {
            LOGGER.warn("Authentication failed for trainee: {}", username);
            return false;
        }
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setPassword(newPassword);
            traineeDAO.save(trainee); 
            LOGGER.info("Password changed successfully for trainee: {}", username);
            return true;
        }
        return false;
    }

    public Optional<Trainee> updateTraineeProfile(String username, String firstName, String lastName, LocalDate dateOfBirth, String address, boolean isActive) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setFirstName(firstName);
            trainee.setLastName(lastName);
            trainee.setDateOfBirth(dateOfBirth);
            trainee.setAddress(address);
            trainee.setActive(isActive);
            LOGGER.info("Trainee profile updated for: {}", username);
            return Optional.of(trainee);
        }
        return Optional.empty();
    }

    public boolean activateDeactivateTrainee(String username, boolean isActive) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setActive(isActive);
            LOGGER.info("Trainee {} status set to: {}", username, isActive ? "ACTIVE" : "INACTIVE");
            return true;
        }
        return false;
    }

    public boolean deleteTraineeProfileByUsername(String username) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            traineeDAO.delete(traineeOpt.get());
            LOGGER.info("Trainee profile deleted for: {}", username);
            return true;
        }
        return false;
    }

    public List<Object[]> getTraineeTrainingsList(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingType) {
        String jpql = "SELECT t.trainingName, t.trainingDate, t.trainingType.trainingTypeName, tr.username FROM Training t JOIN t.trainer tr WHERE t.trainee.username = :username";

        if (fromDate != null) jpql += " AND t.trainingDate >= :fromDate";
        if (toDate != null) jpql += " AND t.trainingDate <= :toDate";
        if (trainerName != null && !trainerName.isEmpty()) jpql += " AND tr.firstName = :trainerName"; 
        if (trainingType != null && !trainingType.isEmpty()) jpql += " AND t.trainingType.trainingTypeName = :trainingType";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isEmpty()) query.setParameter("trainerName", trainerName);
        if (trainingType != null && !trainingType.isEmpty()) query.setParameter("trainingType", trainingType);

        return query.getResultList();
    }

    public List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername) {
        return traineeDAO.findByUsername(traineeUsername)
                .map(trainee -> {
                    List<Long> assignedTrainerIds = trainee.getTrainers().stream().map(Trainer::getId).collect(Collectors.toList());
                    if (assignedTrainerIds.isEmpty()) {
                        return trainerDAO.findAll();
                    }
                    return entityManager.createQuery("SELECT t FROM Trainer t WHERE t.id NOT IN :assignedTrainerIds", Trainer.class)
                            .setParameter("assignedTrainerIds", assignedTrainerIds)
                            .getResultList();
                })
                .orElse(List.of());
    }

    public Optional<List<Trainer>> updateTraineeTrainersList(String username, List<String> trainerUsernames) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            List<Trainer> trainers = entityManager.createQuery("SELECT t FROM Trainer t WHERE t.username IN :usernames", Trainer.class)
                    .setParameter("usernames", trainerUsernames)
                    .getResultList();
            
            trainee.setTrainers(trainers);
            LOGGER.info("Updated trainer list for trainee: {}", username);
            return Optional.of(trainee.getTrainers());
        }
        return Optional.empty();
    }
}