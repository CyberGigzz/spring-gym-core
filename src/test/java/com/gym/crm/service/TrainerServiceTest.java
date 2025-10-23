package com.gym.crm.service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; // Correct DTO import
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Import PasswordEncoder

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
    @Mock
    private PasswordEncoder passwordEncoder; // <-- Mock PasswordEncoder

    @InjectMocks
    private TrainerService trainerService;

    private Trainer testTrainer;
    private TrainingType testSpecialization;
    private String encodedPassword = "encodedOldPassword"; // Simulate encoded password

    @BeforeEach
    void setUp() {
        testSpecialization = new TrainingType();
        testSpecialization.setId(1L);
        testSpecialization.setTrainingTypeName("Cardio");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUsername("test.trainer");
        testTrainer.setPassword(encodedPassword); // Store encoded password
        testTrainer.setFirstName("Test");
        testTrainer.setLastName("Trainer");
        testTrainer.setSpecialization(testSpecialization);
        testTrainer.setActive(true);
    }

    // --- Create Trainer Tests ---

    @Test
    void createTrainerProfile_ShouldGenerateUsernameAndPasswordAndSave() {
        // Arrange
        String firstName = "New";
        String lastName = "Trainer";
        String expectedUsername = "new.trainer";
        String plainPassword = "randomPass"; // Plain password generated
        String expectedEncodedPassword = "encodedRandomPass"; // What encoder returns

        when(userService.generateUsername(firstName, lastName)).thenReturn(expectedUsername);
        when(userService.generatePlainPassword()).thenReturn(plainPassword); // Mock plain generation
        when(userService.encodePassword(plainPassword)).thenReturn(expectedEncodedPassword); // Mock encoding

        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerDAO.save(trainerCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CredentialsDto credentials = trainerService.createTrainerProfile(firstName, lastName, testSpecialization);

        // Assert on returned CredentialsDto
        assertNotNull(credentials);
        assertEquals(expectedUsername, credentials.getUsername());
        assertEquals(plainPassword, credentials.getPassword()); // Should return plain password

        // Verify mocks
        verify(userService, times(1)).generateUsername(firstName, lastName);
        verify(userService, times(1)).generatePlainPassword();
        verify(userService, times(1)).encodePassword(plainPassword);
        verify(trainerDAO, times(1)).save(any(Trainer.class));

        // Assert on captured Trainer
        Trainer capturedTrainer = trainerCaptor.getValue();
        assertEquals(expectedUsername, capturedTrainer.getUsername());
        assertEquals(expectedEncodedPassword, capturedTrainer.getPassword()); // Check ENCODED saved
        assertEquals(firstName, capturedTrainer.getFirstName());
        assertEquals(lastName, capturedTrainer.getLastName());
        assertEquals(testSpecialization, capturedTrainer.getSpecialization());
        assertTrue(capturedTrainer.isActive());
    }

    // --- Check Credentials Tests ---

    @Test
    void checkTrainerCredentials_ShouldReturnTrue_WhenCorrect() {
        // Arrange
        String plainPassword = "oldPassword";
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(plainPassword, encodedPassword)).thenReturn(true); // Mock match success

        // Act & Assert
        assertTrue(trainerService.checkTrainerCredentials("test.trainer", plainPassword));
        verify(passwordEncoder, times(1)).matches(plainPassword, encodedPassword);
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenPasswordIncorrect() {
        // Arrange
        String wrongPlainPassword = "wrongPassword";
        when(trainerDAO.findByUsername("test.trainer")).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(wrongPlainPassword, encodedPassword)).thenReturn(false); // Mock match failure

        // Act & Assert
        assertFalse(trainerService.checkTrainerCredentials("test.trainer", wrongPlainPassword));
        verify(passwordEncoder, times(1)).matches(wrongPlainPassword, encodedPassword);
    }

    @Test
    void checkTrainerCredentials_ShouldReturnFalse_WhenTrainerNotFound() {
        // Arrange
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertFalse(trainerService.checkTrainerCredentials(username, "anyPassword"));
        // Verify encoder was never called
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // --- Change Password Tests ---

    @Test
    void changeTrainerPassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        // Arrange
        String username = "test.trainer";
        String oldPasswordPlain = "oldPassword";
        String newPasswordPlain = "newPassword";
        String newPasswordEncoded = "encodedNewPassword";

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(oldPasswordPlain, encodedPassword)).thenReturn(true); // Old matches
        when(passwordEncoder.encode(newPasswordPlain)).thenReturn(newPasswordEncoded); // Encoding new

        // Act
        boolean result = trainerService.changeTrainerPassword(username, oldPasswordPlain, newPasswordPlain);

        // Assert
        assertTrue(result);
        assertEquals(newPasswordEncoded, testTrainer.getPassword()); // Check ENCODED set

        // Verify encoder interactions
        verify(passwordEncoder, times(1)).matches(oldPasswordPlain, encodedPassword);
        verify(passwordEncoder, times(1)).encode(newPasswordPlain);
        // NO verify(save)
    }

    @Test
    void changeTrainerPassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        // Arrange
        String username = "test.trainer";
        String wrongOldPasswordPlain = "wrongPassword";
        String newPasswordPlain = "newPassword";

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        when(passwordEncoder.matches(wrongOldPasswordPlain, encodedPassword)).thenReturn(false); // Old doesn't match

        // Act
        boolean result = trainerService.changeTrainerPassword(username, wrongOldPasswordPlain, newPasswordPlain);

        // Assert
        assertFalse(result);
        assertEquals(encodedPassword, testTrainer.getPassword()); // Password NOT changed

        // Verify interactions
        verify(passwordEncoder, times(1)).matches(wrongOldPasswordPlain, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString());
        verify(trainerDAO, never()).save(any(Trainer.class));
    }

    @Test
    void changeTrainerPassword_ShouldFail_WhenTrainerNotFound() {
        // Arrange
        String username = "nonexistent.trainer";
        String oldPasswordPlain = "oldPassword";
        String newPasswordPlain = "newPassword";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = trainerService.changeTrainerPassword(username, oldPasswordPlain, newPasswordPlain);

        // Assert
        assertFalse(result);
        // Verify no interactions
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(trainerDAO, never()).save(any(Trainer.class));
    }

    // --- Update Profile Tests ---

    @Test
    void updateTrainerProfile_ShouldUpdateFieldsAndReturnUpdatedTrainer() {
        // Arrange
        String username = "test.trainer";
        String updatedFirstName = "UpdatedFirst";
        String updatedLastName = "UpdatedLast";
        TrainingType newSpecialization = new TrainingType();
        newSpecialization.setId(2L);
        newSpecialization.setTrainingTypeName("Yoga");
        boolean updatedIsActive = false;

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));

        // Act
        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                username, updatedFirstName, updatedLastName, newSpecialization, updatedIsActive);

        // Assert
        assertTrue(updatedTrainerOpt.isPresent());
        Trainer updatedTrainer = updatedTrainerOpt.get();
        assertEquals(updatedFirstName, updatedTrainer.getFirstName());
        assertEquals(updatedLastName, updatedTrainer.getLastName());
        assertEquals(newSpecialization, updatedTrainer.getSpecialization());
        assertEquals(updatedIsActive, updatedTrainer.isActive());
        // NO verify(save)
    }

    @Test
    void updateTrainerProfile_ShouldReturnEmpty_WhenTrainerNotFound() {
        // Arrange
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<Trainer> updatedTrainerOpt = trainerService.updateTrainerProfile(
                username, "Any", "Any", testSpecialization, true);

        // Assert
        assertTrue(updatedTrainerOpt.isEmpty());
        // NO verify(save)
    }

    // --- Activate/Deactivate Tests ---

     @Test
    void activateDeactivateTrainer_ShouldChangeActiveStatusToFalse() {
        // Arrange
        String username = "test.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        assertTrue(testTrainer.isActive());

        // Act
        boolean result = trainerService.activateDeactivateTrainer(username, false);

        // Assert
        assertTrue(result);
        assertFalse(testTrainer.isActive());
        // NO verify(save)
    }

     @Test
    void activateDeactivateTrainer_ShouldChangeActiveStatusToTrue() {
        // Arrange
        String username = "test.trainer";
        testTrainer.setActive(false);
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(testTrainer));
        assertFalse(testTrainer.isActive());

        // Act
        boolean result = trainerService.activateDeactivateTrainer(username, true);

        // Assert
        assertTrue(result);
        assertTrue(testTrainer.isActive());
        // NO verify(save)
    }

    @Test
    void activateDeactivateTrainer_ShouldFail_WhenTrainerNotFound() {
        // Arrange
        String username = "nonexistent.trainer";
        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = trainerService.activateDeactivateTrainer(username, false);

        // Assert
        assertFalse(result);
        // NO verify(save)
    }

    // TODO: Add tests for getTrainerTrainingsList (requires mocking EntityManager)
}