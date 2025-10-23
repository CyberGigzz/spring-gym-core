package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; // Correct DTO import
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Import PasswordEncoder

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder; // <-- Mock PasswordEncoder

    @InjectMocks
    private TraineeService traineeService;

    private Trainee testTrainee;
    private String encodedPassword = "encodedOldPassword"; // Simulate encoded password

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUsername("test.user");
        testTrainee.setPassword(encodedPassword); // Store encoded password in the test object
        testTrainee.setFirstName("Test");
        testTrainee.setLastName("User");
        testTrainee.setActive(true);
        testTrainee.setDateOfBirth(LocalDate.of(1995, 5, 15));
        testTrainee.setAddress("123 Test St");
    }

    // --- Create Trainee Tests ---

    @Test
    void createTraineeProfile_ShouldSucceedAndReturnCredentials() {
        // Arrange
        String firstName = "New";
        String lastName = "User";
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String address = "456 Main St";
        String expectedUsername = "new.user";
        String plainPassword = "randomPass123"; // Plain password generated
        String expectedEncodedPassword = "encodedRandomPass123"; // What encoder returns

        when(userService.generateUsername(firstName, lastName)).thenReturn(expectedUsername);
        when(userService.generatePlainPassword()).thenReturn(plainPassword); // Mock plain password generation
        when(userService.encodePassword(plainPassword)).thenReturn(expectedEncodedPassword); // Mock encoding

        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        when(traineeDAO.save(traineeCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CredentialsDto credentials = traineeService.createTraineeProfile(firstName, lastName, dob, address);

        // Assert on the returned CredentialsDto
        assertNotNull(credentials);
        assertEquals(expectedUsername, credentials.getUsername());
        assertEquals(plainPassword, credentials.getPassword()); // Should return plain password

        // Verify mocks
        verify(userService, times(1)).generateUsername(firstName, lastName);
        verify(userService, times(1)).generatePlainPassword();
        verify(userService, times(1)).encodePassword(plainPassword);
        verify(traineeDAO, times(1)).save(any(Trainee.class));

        // Assert on the captured Trainee before save
        Trainee capturedTrainee = traineeCaptor.getValue();
        assertEquals(expectedUsername, capturedTrainee.getUsername());
        assertEquals(expectedEncodedPassword, capturedTrainee.getPassword()); // Check ENCODED password was saved
        assertEquals(firstName, capturedTrainee.getFirstName());
        assertEquals(lastName, capturedTrainee.getLastName());
        assertEquals(dob, capturedTrainee.getDateOfBirth());
        assertEquals(address, capturedTrainee.getAddress());
        assertTrue(capturedTrainee.isActive());
    }

    // --- Change Password Tests ---

    @Test
    void changeTraineePassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        // Arrange
        String username = "test.user";
        String oldPasswordPlain = "oldPassword"; // Plain text old password
        String newPasswordPlain = "newPassword"; // Plain text new password
        String newPasswordEncoded = "encodedNewPassword"; // Encoded new password

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        // Mock password encoder checks
        when(passwordEncoder.matches(oldPasswordPlain, encodedPassword)).thenReturn(true); // Old password matches
        when(passwordEncoder.encode(newPasswordPlain)).thenReturn(newPasswordEncoded); // Encoding the new one

        // Act
        boolean result = traineeService.changeTraineePassword(username, oldPasswordPlain, newPasswordPlain);

        // Assert
        assertTrue(result);
        assertEquals(newPasswordEncoded, testTrainee.getPassword()); // Verify ENCODED password was set

        // Verify encoder interactions
        verify(passwordEncoder, times(1)).matches(oldPasswordPlain, encodedPassword);
        verify(passwordEncoder, times(1)).encode(newPasswordPlain);
        // NO verify(save) due to dirty checking
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        // Arrange
        String username = "test.user";
        String wrongOldPasswordPlain = "wrongOldPassword";
        String newPasswordPlain = "newPassword";

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        // Mock password encoder check to fail
        when(passwordEncoder.matches(wrongOldPasswordPlain, encodedPassword)).thenReturn(false);

        // Act
        boolean result = traineeService.changeTraineePassword(username, wrongOldPasswordPlain, newPasswordPlain);

        // Assert
        assertFalse(result);
        assertEquals(encodedPassword, testTrainee.getPassword()); // Password should NOT change

        // Verify encoder interaction
        verify(passwordEncoder, times(1)).matches(wrongOldPasswordPlain, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString()); // Encode should not be called
        verify(traineeDAO, never()).save(any(Trainee.class)); // Save should not be called
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenTraineeNotFound() {
        // Arrange
        String username = "nonexistent.user";
        String oldPasswordPlain = "oldPassword";
        String newPasswordPlain = "newPassword";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = traineeService.changeTraineePassword(username, oldPasswordPlain, newPasswordPlain);

        // Assert
        assertFalse(result);

        // Verify encoder was never called
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(traineeDAO, never()).save(any(Trainee.class));
    }

    // --- Activate/Deactivate Tests ---

    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatusToFalse() {
        // Arrange
        String username = "test.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        assertTrue(testTrainee.isActive());

        // Act
        boolean result = traineeService.activateDeactivateTrainee(username, false);

        // Assert
        assertTrue(result);
        assertFalse(testTrainee.isActive());
        // NO verify(save) needed
    }

    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatusToTrue() {
        // Arrange
        String username = "test.user";
        testTrainee.setActive(false); // Start inactive
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        assertFalse(testTrainee.isActive());

        // Act
        boolean result = traineeService.activateDeactivateTrainee(username, true);

        // Assert
        assertTrue(result);
        assertTrue(testTrainee.isActive());
        // NO verify(save) needed
    }

    @Test
    void activateDeactivateTrainee_ShouldFail_WhenTraineeNotFound() {
        // Arrange
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = traineeService.activateDeactivateTrainee(username, false);

        // Assert
        assertFalse(result);
        // NO verify(save) needed
    }

    // --- Update Profile Tests ---

    @Test
    void updateTraineeProfile_ShouldSucceedAndReturnUpdatedTrainee() {
        // Arrange
        String username = "test.user";
        String updatedFirstName = "UpdatedJohn";
        String updatedLastName = "UpdatedDoe";
        LocalDate updatedDob = LocalDate.of(1992, 2, 2);
        String updatedAddress = "456 Updated St";
        boolean updatedIsActive = false;

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));

        // Act
        Optional<Trainee> resultOpt = traineeService.updateTraineeProfile(username, updatedFirstName, updatedLastName, updatedDob, updatedAddress, updatedIsActive);

        // Assert
        assertTrue(resultOpt.isPresent());
        Trainee updatedTrainee = resultOpt.get();
        assertEquals(updatedFirstName, updatedTrainee.getFirstName());
        assertEquals(updatedLastName, updatedTrainee.getLastName());
        assertEquals(updatedDob, updatedTrainee.getDateOfBirth());
        assertEquals(updatedAddress, updatedTrainee.getAddress());
        assertEquals(updatedIsActive, updatedTrainee.isActive());
        // NO verify(save) needed
    }

    @Test
    void updateTraineeProfile_ShouldReturnEmpty_WhenTraineeNotFound() {
        // Arrange
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<Trainee> resultOpt = traineeService.updateTraineeProfile(username, "New", "Name", null, null, true);

        // Assert
        assertTrue(resultOpt.isEmpty());
        // NO verify(save) needed
    }

    // --- Delete Profile Tests ---

    @Test
    void deleteTraineeProfileByUsername_ShouldSucceed_WhenTraineeExists() {
        // Arrange
        String username = "test.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeDAO).delete(testTrainee); // Mock the void delete method

        // Act
        boolean result = traineeService.deleteTraineeProfileByUsername(username);

        // Assert
        assertTrue(result);

        // Verify delete was called
        verify(traineeDAO, times(1)).delete(testTrainee);
    }

    @Test
    void deleteTraineeProfileByUsername_ShouldFail_WhenTraineeNotFound() {
        // Arrange
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        boolean result = traineeService.deleteTraineeProfileByUsername(username);

        // Assert
        assertFalse(result);

        // Verify delete was NOT called
        verify(traineeDAO, never()).delete(any(Trainee.class));
    }

    // REMOVED duplicate tests from the end of the previous file

    // TODO: Add tests for getTraineeTrainingsList, getUnassignedTrainersForTrainee, updateTraineeTrainersList (these require mocking EntityManager or more complex setups)
}