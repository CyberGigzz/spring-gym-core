package com.gym.crm.service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; // Import DTO
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Import
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
    private final PasswordEncoder passwordEncoder; // <-- Add field

    @PersistenceContext
    private EntityManager entityManager;

    public TrainerService(UserService userService, TrainerDAO trainerDAO, PasswordEncoder passwordEncoder) { // <-- Inject here
        this.userService = userService;
        this.trainerDAO = trainerDAO;
        this.passwordEncoder = passwordEncoder; // <-- Assign
    }

    // Return CredentialsDto instead of Trainer
    public CredentialsDto createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);
        trainer.setActive(true);

        String username = userService.generateUsername(firstName, lastName);
        String plainPassword = userService.generatePlainPassword(); // Generate plain password
        String encodedPassword = userService.encodePassword(plainPassword); // Encode it

        trainer.setUsername(username);
        trainer.setPassword(encodedPassword); // Store encoded password

        trainerDAO.save(trainer);
        LOGGER.info("Successfully created trainer with username: {}", username);

        // Return DTO with plain password
        CredentialsDto credentials = new CredentialsDto();
        credentials.setUsername(username);
        credentials.setPassword(plainPassword); // Return plain one
        return credentials;
    }

    @Transactional(readOnly = true) // Add readOnly
    public boolean checkTrainerCredentials(String username, String plainPassword) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        // Use matches()
        return trainerOpt.map(trainer -> passwordEncoder.matches(plainPassword, trainer.getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true) // Add readOnly
    public Optional<Trainer> selectTrainerProfileByUsername(String username) {
        return trainerDAO.findByUsername(username);
    }

    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            // Check old password using matches()
            if (passwordEncoder.matches(oldPassword, trainer.getPassword())) {
                // Encode the new password
                trainer.setPassword(passwordEncoder.encode(newPassword));
                // No need to call save
                LOGGER.info("Password changed successfully for trainer: {}", username);
                return true;
            } else {
                LOGGER.warn("Authentication failed for trainer (incorrect old password): {}", username);
                return false;
            }
        }
        LOGGER.warn("Trainer not found for password change: {}", username);
        return false;
    }

    public Optional<Trainer> updateTrainerProfile(String username, String firstName, String lastName, TrainingType specialization, boolean isActive) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            trainer.setFirstName(firstName);
            trainer.setLastName(lastName);
            trainer.setSpecialization(specialization); // Assuming specialization can be updated
            trainer.setActive(isActive);
            // No need to call save
            LOGGER.info("Trainer profile updated for: {}", username);
            return Optional.of(trainer);
        }
        LOGGER.warn("Trainer not found for profile update: {}", username);
        return Optional.empty();
    }

    public boolean activateDeactivateTrainer(String username, boolean isActive) {
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(username);
        if (trainerOpt.isPresent()) {
            Trainer trainer = trainerOpt.get();
            trainer.setActive(isActive);
            // No need to call save
            LOGGER.info("Trainer {} status set to: {}", username, isActive ? "ACTIVE" : "INACTIVE");
            return true;
        }
        LOGGER.warn("Trainer not found for status change: {}", username);
        return false;
    }

    @Transactional(readOnly = true) // Add readOnly
    public List<Object[]> getTrainerTrainingsList(String username, LocalDate fromDate, LocalDate toDate, String traineeName) {
        // Ensure query selects duration
        String jpql = "SELECT t.trainingName, t.trainingDate, t.trainingType.trainingTypeName, t.trainingDuration, te.username FROM Training t JOIN t.trainee te WHERE t.trainer.username = :username";

        if (fromDate != null) jpql += " AND t.trainingDate >= :fromDate";
        if (toDate != null) jpql += " AND t.trainingDate <= :toDate";
        // Corrected parameter name for trainee name filtering
        if (traineeName != null && !traineeName.isEmpty()) jpql += " AND (te.firstName = :traineeName OR te.lastName = :traineeName)";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isEmpty()) query.setParameter("traineeName", traineeName);

        return query.getResultList();
    }
}