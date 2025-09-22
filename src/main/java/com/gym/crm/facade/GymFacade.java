package com.gym.crm.facade;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee registerTrainee(String firstName, String lastName, LocalDate dob, String address) {
        return traineeService.createTraineeProfile(firstName, lastName, dob, address);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.selectTraineeProfile(id);
    }

    public Trainer registerTrainer(String firstName, String lastName, TrainingType specialization) {
        return trainerService.createTrainerProfile(firstName, lastName, specialization);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.selectTrainerProfile(id);
    }

    public Training addTraining(Long traineeId, Long trainerId, String name, TrainingType type, LocalDate date, int duration) {
        return trainingService.createTraining(traineeId, trainerId, name, type, date, duration);
    }
}