package com.gym.crm;

import com.gym.crm.config.AppConfig;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.storage.Storage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext; 

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Gym CRM application...");

        try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            System.out.println("Spring context initialized successfully.");

            System.out.println("\n----- VERIFYING INITIAL DATA -----");
            Storage storage = context.getBean(Storage.class);
            System.out.println("Initial Trainees: " + storage.getTraineeStorage().values());
            System.out.println("Initial Trainers: " + storage.getTrainerStorage().values());

            System.out.println("\n----- USING THE FACADE TO INTERACT WITH THE SYSTEM -----");
            GymFacade facade = context.getBean(GymFacade.class);

            System.out.println("\nRegistering a new trainee 'Peter Jones'...");
            Trainee peter = facade.registerTrainee("Peter", "Jones", LocalDate.of(2000, 1, 15), "456 Oak Ave");
            System.out.println("Created Trainee: " + peter);

            System.out.println("\nRegistering a trainee with a conflicting name 'John Doe'...");
            Trainee anotherJohn = facade.registerTrainee("John", "Doe", LocalDate.of(1998, 10, 5), "789 Pine Ln");
            System.out.println("Created Trainee with conflicting name: " + anotherJohn);
            System.out.println("===> Note the username is 'john.doe1', which is correct!");

            System.out.println("\nRegistering a new trainer 'Mary Garcia'...");
            TrainingType yoga = new TrainingType();
            yoga.setId(3L);
            yoga.setTrainingTypeName("Yoga");
            Trainer mary = facade.registerTrainer("Mary", "Garcia", yoga);
            System.out.println("Created Trainer: " + mary);

        } 

        System.out.println("\nApplication finished and context closed.");
    }
}