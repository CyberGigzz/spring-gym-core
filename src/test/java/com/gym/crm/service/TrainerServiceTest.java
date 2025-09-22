package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TraineeDAO traineeDAO; 

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService();
        trainerService.setTrainerDAO(trainerDAO);
        trainerService.setTraineeDAO(traineeDAO);
    }

    private TrainingType createTestSpecialization() {
        TrainingType specialization = new TrainingType();
        specialization.setId(1L);
        specialization.setTrainingTypeName("Fitness");
        return specialization;
    }

    @Test
    void createTrainerProfile_ShouldGenerateUniqueUsername() {
        String firstName = "Mary";
        String lastName = "Garcia";
        when(trainerDAO.findAll()).thenReturn(Collections.emptyList());
        when(traineeDAO.findAll()).thenReturn(Collections.emptyList());

        Trainer newTrainer = trainerService.createTrainerProfile(firstName, lastName, createTestSpecialization());

        assertNotNull(newTrainer);
        assertEquals("mary.garcia", newTrainer.getUsername());
        assertNotNull(newTrainer.getPassword());
        assertEquals(10, newTrainer.getPassword().length());
        assertTrue(newTrainer.isActive());
        assertEquals("Fitness", newTrainer.getSpecialization().getTrainingTypeName());

        verify(trainerDAO, times(1)).save(any(Trainer.class));
    }

    @Test
    void createTrainerProfile_ShouldHandleUsernameCollisionWithTrainee() {
        String firstName = "John";
        String lastName = "Doe";

        Trainee existingTrainee = new Trainee();
        existingTrainee.setUsername("john.doe");
        when(traineeDAO.findAll()).thenReturn(Collections.singletonList(existingTrainee));
        when(trainerDAO.findAll()).thenReturn(Collections.emptyList());

        Trainer newTrainer = trainerService.createTrainerProfile(firstName, lastName, createTestSpecialization());

        assertEquals("john.doe1", newTrainer.getUsername());

        verify(trainerDAO, times(1)).save(any(Trainer.class));
    }

    @Test
    void selectTrainerProfile_ShouldReturnTrainer_WhenIdExists() {
        Trainer mockTrainer = new Trainer();
        mockTrainer.setId(1L);
        mockTrainer.setUsername("test.trainer");
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(mockTrainer));

        Optional<Trainer> foundTrainer = trainerService.selectTrainerProfile(1L);

        assertTrue(foundTrainer.isPresent());
        assertEquals("test.trainer", foundTrainer.get().getUsername());

        verify(trainerDAO, times(1)).findById(1L);
    }

    @Test
    void updateTrainerProfile_ShouldCallSaveOnDao() {
        Trainer trainerToUpdate = new Trainer();
        trainerToUpdate.setId(1L);
        trainerToUpdate.setUsername("existing.trainer");

        trainerService.updateTrainerProfile(trainerToUpdate);

        verify(trainerDAO, times(1)).save(trainerToUpdate);
    }
}