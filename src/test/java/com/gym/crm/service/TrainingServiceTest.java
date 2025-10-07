package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDAO trainingDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUsername("test.trainee");

        testTrainer = new Trainer();
        testTrainer.setId(2L);
        testTrainer.setUsername("test.trainer");

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");
    }

    @Test
    void addTraining_ShouldSucceed_WhenTraineeAndTrainerExist() {
        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        Training result = trainingService.addTraining(
                "test.trainee", "test.trainer", "Morning Yoga",
                testTrainingType, LocalDate.now(), 60);

        assertNotNull(result);
        assertEquals(testTrainee, result.getTrainee());
        assertEquals(testTrainer, result.getTrainer());
        assertEquals("Morning Yoga", result.getTrainingName());

        verify(trainingDAO, times(1)).save(any(Training.class));
    }

    @Test
    void addTraining_ShouldReturnNull_WhenTraineeNotFound() {
        when(traineeDAO.findByUsername("unknown.trainee")).thenReturn(Optional.empty());
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer)); 

        Training result = trainingService.addTraining(
                "unknown.trainee", "test.trainer", "Morning Yoga",
                testTrainingType, LocalDate.now(), 60);

        assertNull(result);

        verify(trainingDAO, never()).save(any(Training.class));
    }

    @Test
    void addTraining_ShouldReturnNull_WhenTrainerNotFound() {
        when(traineeDAO.findByUsername("test.trainee")).thenReturn(Optional.of(testTrainee)); 
        when(trainerDAO.findByUsername("unknown.trainer")).thenReturn(Optional.empty());

        Training result = trainingService.addTraining(
                "test.trainee", "unknown.trainer", "Morning Yoga",
                testTrainingType, LocalDate.now(), 60);

        assertNull(result);

        verify(trainingDAO, never()).save(any(Training.class));
    }
}