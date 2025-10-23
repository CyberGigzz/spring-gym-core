package com.gym.crm.service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        testTrainer.setFirstName("Test");
        testTrainer.setLastName("Trainer");
        testTrainer.setSpecialization(testSpecialization);
        testTrainer.setActive(true);
    }

    @Test
    void createTrainerProfile_ShouldGenerateUsernameAndPasswordAndSave() {
        String firstName = "New";
        String lastName = "Trainer";
        String expectedUsername = "new.trainer";
        String expectedPassword = "randomPass";

        when(userService.generateUsername(firstName, lastName)).thenReturn(expectedUsername);
        when(userService.generateRandomPassword()).thenReturn(expectedPassword);
        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerDAO.save(trainerCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer newTrainer = trainerService.createTrainerProfile(firstName, lastName, testSpecialization);

        assertNotNull(newTrainer);
        assertEquals(expectedUsername, newTrainer.getUsername());
        assertEquals(expectedPassword, newTrainer.getPassword());
        assertEquals(firstName, newTrainer.getFirstName());
        assertEquals(lastName, newTrainer.getLastName());
        assertEquals(testSpecialization, newTrainer.getSpecialization());
        assertTrue(newTrainer.isActive());

        verify(userService, times(1)).generateUsername(firstName, lastName);
        verify(userService, times(1)).generateRandomPassword();
        verify(trainerDAO, times(1)).save(any(Trainer.class)); 

        Trainer capturedTrainer = trainerCaptor.getValue();
        assertEquals(expectedUsername, capturedTrainer.getUsername());
        assertEquals(expectedPassword, capturedTrainer.getPassword());
        assertTrue(capturedTrainer.isActive());
        assertEquals(testSpecialization, capturedTrainer.getSpecialization());
    }

    @Test
    void checkTrainerCredentials_ShouldReturnTrue_WhenCorrect() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        assertTrue(trainerService.checkTrainerCredentials("test.trainer", "oldPassword"));
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenPasswordIncorrect() {
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));

        assertFalse(trainerService.checkTrainerCredentials("test.trainer", "wrongPassword"));
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenTrainerNotFound() {
        when(trainerDAO.findByUsername("nonexistent.trainer")).thenReturn(Optional.empty());

        assertFalse(trainerService.checkTrainerCredentials("nonexistent.trainer", "anyPassword"));
    }

    @Test
    void changeTrainerPassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        String username = "test.trainer";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));

        boolean result = trainerService.changeTrainerPassword(username, oldPassword, newPassword);

        assertTrue(result);
        assertEquals(newPassword, testTrainer.getPassword()); 

    }

    @Test
    void changeTrainerPassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        String username = "test.trainer";
        String wrongOldPassword = "wrongPassword";
        String newPassword = "newPassword";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));

        boolean result = trainerService.changeTrainerPassword(username, wrongOldPassword, newPassword);

        assertFalse(result);
        assertEquals("oldPassword", testTrainer.getPassword()); 

        verify(trainerDAO, never()).save(any(Trainer.class));
    }

    @Test
    void changeTrainerPassword_ShouldFail_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = trainerService.changeTrainerPassword(username, oldPassword, newPassword);

        assertFalse(result);

        verify(trainerDAO, never()).save(any(Trainer.class));
    }

    @Test
    void updateTrainerProfile_ShouldUpdateFieldsAndReturnUpdatedTrainer() {
        String username = "test.trainer";
        String updatedFirstName = "UpdatedFirst";
        String updatedLastName = "UpdatedLast";
        TrainingType newSpecialization = new TrainingType(); 
        newSpecialization.setId(2L);
        newSpecialization.setTrainingTypeName("Yoga");
        boolean updatedIsActive = false;

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                username, updatedFirstName, updatedLastName, newSpecialization, updatedIsActive);

        assertTrue(updatedTrainerOpt.isPresent());
        Trainer updatedTrainer = updatedTrainerOpt.get(); 
        assertEquals(updatedFirstName, updatedTrainer.getFirstName());
        assertEquals(updatedLastName, updatedTrainer.getLastName());
        assertEquals(newSpecialization, updatedTrainer.getSpecialization());
        assertEquals(updatedIsActive, updatedTrainer.isActive());

    }

    @Test
    void updateTrainerProfile_ShouldReturnEmpty_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                username, "Any", "Any", testSpecialization, true);

        assertTrue(updatedTrainerOpt.isEmpty());

        verify(trainerDAO, never()).save(any(Trainer.class));
    }

     @Test
    void activateDeactivateTrainer_ShouldChangeActiveStatusToFalse() {
        String username = "test.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        assertTrue(testTrainer.isActive()); 

        boolean result = trainerService.activateDeactivateTrainer(username, false); 

        assertTrue(result);
        assertFalse(testTrainer.isActive()); 

    }

     @Test
    void activateDeactivateTrainer_ShouldChangeActiveStatusToTrue() {
        String username = "test.trainer";
        testTrainer.setActive(false); 
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        assertFalse(testTrainer.isActive()); 

        boolean result = trainerService.activateDeactivateTrainer(username, true); 

        assertTrue(result);
        assertTrue(testTrainer.isActive()); 

    }

    @Test
    void activateDeactivateTrainer_ShouldFail_WhenTrainerNotFound() {
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = trainerService.activateDeactivateTrainer(username, false);

        assertFalse(result);

        verify(trainerDAO, never()).save(any(Trainer.class));
    }

}