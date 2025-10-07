package com.gym.crm.service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
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

@Service
@Transactional
public class TrainerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerService.class);
    private final UserService userService;
    private final TrainerDAO trainerDAO;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TrainerService(UserService userService, TrainerDAO trainerDAO) {
        this.userService = userService;
        this.trainerDAO = trainerDAO;
    }

    public Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        trainer.setActive(true);

        String username = userService.generateUsername(firstName, lastName);
        String password = userService.generateRandomPassword();
        trainer.setUsername(username);
        trainer.setPassword(password);

        trainerDAO.save(trainer);
        LOGGER.info("Successfully created trainer with username: {}", username);
        return trainer;
    }

    public boolean checkTrainerCredentials(String username, String password) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        return trainerOpt.map(trainer -> trainer.getPassword().equals(password)).orElse(false);
    }

    public Optional<Trainer> selectTrainerProfileByUsername(String username) {
        return trainerDAO.findByUsername(username);
    }

    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        if (!checkTrainerCredentials(username, oldPassword)) {
            LOGGER.warn("Authentication failed for trainer: {}", username);
            return false;
        }
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            trainer.setPassword(newPassword);
            LOGGER.info("Password changed successfully for trainer: {}", username);
            return true;
        }
        return false;
    }

    public Optional<Trainer> updateTrainerProfile(String username, String firstName, String lastName, TrainingType specialization, boolean isActive) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            trainer.setFirstName(firstName);
            trainer.setLastName(lastName);
            trainer.setSpecialization(specialization);
            trainer.setActive(isActive);
            LOGGER.info("Trainer profile updated for: {}", username);
            return Optional.of(trainer);
        }
        return Optional.empty();
    }

    public boolean activateDeactivateTrainer(String username, boolean isActive) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            trainer.setActive(isActive);
            LOGGER.info("Trainer {} status set to: {}", username, isActive ? "ACTIVE" : "INACTIVE");
            return true;
        }
        return false;
    }

    public List<Object[]> getTrainerTrainingsList(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        String jpql = "SELECT t.trainingName, t.trainingDate, t.trainingType.trainingTypeName, te.username FROM Training t JOIN t.trainee te WHERE t.trainer.username = :username";

        if (fromDate != null) jpql += " AND t.trainingDate >= :fromDate";
        if (toDate != null) jpql += " AND t.trainingDate <= :toDate";
        if (traineeName != null && !traineeName.isEmpty()) jpql += " AND te.firstName = :traineeName";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isEmpty()) query.setParameter("traineeName", traineeName);

        return query.getResultList();
    }
}