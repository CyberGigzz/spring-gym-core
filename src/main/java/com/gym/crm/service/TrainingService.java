package com.gym.crm.service;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);
    private final AtomicLong idCounter = new AtomicLong(1);

    private TrainingDAO trainingDAO;

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName, TrainingType trainingType, LocalDate trainingDate, int duration) {
        Training training = new Training();
        training.setId(idCounter.getAndIncrement());
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(duration);

        trainingDAO.save(training);
        LOGGER.info("Successfully created training: {}", trainingName);
        return training;
    }

    public Optional<Training> selectTraining(Long id) {
        LOGGER.info("Attempting to select training with ID: {}", id);
        return trainingDAO.findById(id);
    }
}