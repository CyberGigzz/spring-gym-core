package com.gym.crm.service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserService userService;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer testTrainer;
    private TrainingType testSpecialization;

    @BeforeEach
    void setUp() {
        testSpecialization = new TrainingType();
        testSpecialization.setId(1L);
        testSpecialization.setTrainingTypeName("Cardio");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUsername("test.trainer");
        testTrainer.setPassword("oldPassword");
        testTrainer.setSpecialization(testSpecialization);
        testTrainer.setActive(true);
    }

    @Test
    void createTrainerProfile_ShouldGenerateUsernameAndPasswordAndSave() {
        when(userService.generateUsername(anyString(), anyString())).thenReturn("new.trainer");
        when(userService.generateRandomPassword()).thenReturn("randomPass");

        Trainer newTrainer = trainerService.createTrainerProfile("New", "Trainer", testSpecialization);

        assertNotNull(newTrainer);
        assertEquals("new.trainer", newTrainer.getUsername());
        assertEquals("randomPass", newTrainer.getPassword());
        assertTrue(newTrainer.isActive());

        verify(trainerDAO, times(1)).save(any(Trainer.class));
    }

    @Test
    void checkTrainerCredentials_ShouldReturnTrue_WhenCorrect() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        assertTrue(trainerService.checkTrainerCredentials("test.trainer", "oldPassword"));
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenIncorrect() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        assertFalse(trainerService.checkTrainerCredentials("test.trainer", "wrongPassword"));
    }

    @Test
    void changeTrainerPassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        boolean result = trainerService.changeTrainerPassword("test.trainer", "oldPassword", "newPassword");

        assertTrue(result);
        assertEquals("newPassword", testTrainer.getPassword());
    }

    @Test
    void updateTrainerProfile_ShouldUpdateFields() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        TrainingType newSpecialization = new TrainingType();
        newSpecialization.setTrainingTypeName("Yoga");

        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                "test.trainer", "UpdatedFirst", "UpdatedLast", newSpecialization, false);

        assertTrue(updatedTrainerOpt.isPresent());
        Trainer updatedTrainer = updatedTrainerOpt.get();
        assertEquals("UpdatedFirst", updatedTrainer.getFirstName());
        assertEquals("UpdatedLast", updatedTrainer.getLastName());
        assertEquals("Yoga", updatedTrainer.getSpecialization().getTrainingTypeName());
        assertFalse(updatedTrainer.isActive());
    }
}