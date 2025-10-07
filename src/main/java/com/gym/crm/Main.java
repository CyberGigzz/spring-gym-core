package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.dao.TrainingTypeDAO;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);
            TrainingTypeDAO trainingTypeDAO = context.getBean(TrainingTypeDAO.class);

            System.out.println("--- Spring Context Initialized and Database Schema Created ---");

            System.out.println("\n--- 1. CREATING PROFILES ---");

            TrainingType cardio = trainingTypeDAO.findById(1L).orElseThrow(() -> new RuntimeException("Cardio TrainingType not found"));
            TrainingType weightlifting = trainingTypeDAO.findById(2L).orElseThrow(() -> new RuntimeException("Weightlifting TrainingType not found"));

            Trainee john = facade.createTraineeProfile("John", "Doe", LocalDate.of(1995, 5, 20), "123 Main St");
            Trainee jane = facade.createTraineeProfile("Jane", "Roe", LocalDate.of(1998, 8, 15), "456 Oak Ave");
            Trainer trainerMike = facade.createTrainerProfile("Mike", "Johnson", cardio);
            Trainer trainerAnna = facade.createTrainerProfile("Anna", "Williams", weightlifting);

            System.out.println("Created Trainee: " + john.getUsername() + " | Password: " + john.getPassword());
            System.out.println("Created Trainee: " + jane.getUsername() + " | Password: " + jane.getPassword());
            System.out.println("Created Trainer: " + trainerMike.getUsername() + " | Password: " + trainerMike.getPassword());
            System.out.println("Created Trainer: " + trainerAnna.getUsername() + " | Password: " + trainerAnna.getPassword());

            System.out.println("\n--- 2. AUTHENTICATING AND SELECTING PROFILES ---");
            Optional<Trainee> fetchedJohn = facade.getTraineeProfile(john.getUsername(), john.getPassword());
            System.out.println("Successfully fetched John's profile with correct password: " + fetchedJohn.isPresent());
            Optional<Trainee> failedFetch = facade.getTraineeProfile(john.getUsername(), "wrongpassword");
            System.out.println("Fetch with wrong password failed as expected: " + failedFetch.isEmpty());

            System.out.println("\n--- 3. UPDATING PROFILE AND CHANGING PASSWORD ---");
            facade.updateTraineeProfile(john.getUsername(), john.getPassword(), "Johnathan", "Doe", LocalDate.of(1995, 5, 20), "789 Pine Rd", true);
            System.out.println("Updated John's first name and address.");
            boolean passChanged = facade.changeTraineePassword(john.getUsername(), john.getPassword(), "newSecretPass123");
            john.setPassword("newSecretPass123"); 
            System.out.println("John's password changed successfully: " + passChanged);

            System.out.println("\n--- 4. DEACTIVATING A PROFILE ---");
            facade.activateDeactivateTrainee(jane.getUsername(), jane.getPassword(), false);
            Trainee deactivatedJane = facade.getTraineeProfile(jane.getUsername(), jane.getPassword()).orElseThrow();
            System.out.println("Jane's profile has been deactivated. IsActive: " + deactivatedJane.isActive());

            System.out.println("\n--- 5. MANAGING TRAINEE-TRAINER RELATIONSHIPS ---");
            List<String> trainerUsernames = List.of(trainerMike.getUsername(), trainerAnna.getUsername());
            facade.updateTraineeTrainers(john.getUsername(), john.getPassword(), trainerUsernames);
            System.out.println("Assigned trainers Mike and Anna to John.");
            List<Trainer> unassignedToJane = facade.getUnassignedTrainers(jane.getUsername(), jane.getPassword());
            System.out.println("Trainers not assigned to Jane: " + unassignedToJane.size() + " (should be 2)");

            System.out.println("\n--- 6. ADDING TRAININGS ---");
            facade.addTraining(john.getUsername(), john.getPassword(), trainerMike.getUsername(), "Morning Run", cardio, LocalDate.now(), 45);
            facade.addTraining(john.getUsername(), john.getPassword(), trainerAnna.getUsername(), "Leg Day", weightlifting, LocalDate.now().plusDays(1), 60);
            System.out.println("Added two new trainings for John.");

            System.out.println("\n--- 7. GETTING TRAINING LISTS WITH CRITERIA ---");
            List<Object[]> johnsTrainings = facade.getTraineeTrainingsList(john.getUsername(), john.getPassword(), null, null, null, null);
            System.out.println("John's All Trainings (" + johnsTrainings.size() + " total):");
            johnsTrainings.forEach(t -> System.out.println("  - " + Arrays.toString(t)));

            List<Object[]> johnsCardioTrainings = facade.getTraineeTrainingsList(john.getUsername(), john.getPassword(), null, null, null, "Cardio");
            System.out.println("John's Cardio-only Trainings (" + johnsCardioTrainings.size() + " total):");
            johnsCardioTrainings.forEach(t -> System.out.println("  - " + Arrays.toString(t)));

            System.out.println("\n--- 8. DELETING TRAINEE (CASCADE DELETE) ---");
            boolean janeDeleted = facade.deleteTraineeProfile(jane.getUsername(), jane.getPassword());
            System.out.println("Jane's profile was deleted: " + janeDeleted);
            Optional<Trainee> checkJane = facade.getTraineeProfile(jane.getUsername(), jane.getPassword());
            System.out.println("Verifying Jane is gone from the database: " + checkJane.isEmpty());

        } 
    }
}