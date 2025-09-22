package com.gym.crm.service;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService();
        trainingService.setTrainingDAO(trainingDAO);
    }

    @Test
    void createTraining_ShouldCreateAndSaveTraining() {
        Long traineeId = 1L;
        Long trainerId = 2L;
        String trainingName = "Morning Cardio";
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Cardio");
        LocalDate trainingDate = LocalDate.now();
        int duration = 60;

        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);
        when(trainingDAO.save(trainingCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Training createdTraining = trainingService.createTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);

        assertNotNull(createdTraining);
        assertNotNull(createdTraining.getId()); 
        assertEquals(traineeId, createdTraining.getTraineeId());
        assertEquals(trainerId, createdTraining.getTrainerId());
        assertEquals(trainingName, createdTraining.getTrainingName());
        assertEquals("Cardio", createdTraining.getTrainingType().getTrainingTypeName());
        assertEquals(trainingDate, createdTraining.getTrainingDate());
        assertEquals(duration, createdTraining.getTrainingDuration());

        verify(trainingDAO, times(1)).save(any(Training.class));
    }

    @Test
    void selectTraining_ShouldReturnTraining_WhenFound() {
        Training mockTraining = new Training();
        mockTraining.setId(1L);
        mockTraining.setTrainingName("Test Training");

        when(trainingDAO.findById(1L)).thenReturn(Optional.of(mockTraining));

        Optional<Training> result = trainingService.selectTraining(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Training", result.get().getTrainingName());

        verify(trainingDAO, times(1)).findById(1L);
    }

    @Test
    void selectTraining_ShouldReturnEmpty_WhenNotFound() {
        when(trainingDAO.findById(99L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.selectTraining(99L);

        assertFalse(result.isPresent());

        verify(trainingDAO, times(1)).findById(99L);
    }
}