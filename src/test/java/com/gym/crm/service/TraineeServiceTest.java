package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private TraineeService traineeService;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUsername("test.user");
        testTrainee.setPassword("oldPassword");
        testTrainee.setActive(true);
    }

    @Test
    void createTraineeProfile_ShouldSucceedAndReturnCredentials() {
        String firstName = "New";
        String lastName = "User";
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String address = "456 Main St";
        String expectedUsername = "new.user";
        String expectedPassword = "randomPass123";

        when(userService.generateUsername(firstName, lastName)).thenReturn(expectedUsername);
        when(userService.generateRandomPassword()).thenReturn(expectedPassword);
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        when(traineeDAO.save(traineeCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee newTrainee = traineeService.createTraineeProfile(firstName, lastName, dob, address);

        assertNotNull(newTrainee);
        assertEquals(expectedUsername, newTrainee.getUsername());
        assertEquals(expectedPassword, newTrainee.getPassword());
        assertEquals(firstName, newTrainee.getFirstName());
        assertEquals(lastName, newTrainee.getLastName());
        assertEquals(dob, newTrainee.getDateOfBirth());
        assertEquals(address, newTrainee.getAddress());
        assertTrue(newTrainee.isActive());

        verify(userService, times(1)).generateUsername(firstName, lastName);
        verify(userService, times(1)).generateRandomPassword();
        verify(traineeDAO, times(1)).save(any(Trainee.class)); 

        Trainee capturedTrainee = traineeCaptor.getValue();
        assertEquals(expectedUsername, capturedTrainee.getUsername());
        assertEquals(expectedPassword, capturedTrainee.getPassword());
        assertTrue(capturedTrainee.isActive());
    }

    @Test
    void changeTraineePassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        String username = "test.user";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));

        boolean result = traineeService.changeTraineePassword(username, oldPassword, newPassword);

        assertTrue(result);
        assertEquals(newPassword, testTrainee.getPassword()); 

        verify(traineeDAO, times(1)).save(testTrainee);
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        String username = "test.user";
        String wrongOldPassword = "wrongOldPassword";
        String newPassword = "newPassword";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));

        boolean result = traineeService.changeTraineePassword(username, wrongOldPassword, newPassword);

        assertFalse(result);
        assertEquals("oldPassword", testTrainee.getPassword()); 

        verify(traineeDAO, never()).save(any(Trainee.class));
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = traineeService.changeTraineePassword(username, oldPassword, newPassword);

        assertFalse(result);

        verify(traineeDAO, never()).save(any(Trainee.class));
    }

    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatusToFalse() {
        String username = "test.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        assertTrue(testTrainee.isActive()); 

        boolean result = traineeService.activateDeactivateTrainee(username, false); 

        assertTrue(result); 
        assertFalse(testTrainee.isActive()); 

    }

    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatusToTrue() {
        String username = "test.user";
        testTrainee.setActive(false);
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        assertFalse(testTrainee.isActive()); 

        boolean result = traineeService.activateDeactivateTrainee(username, true); 

        assertTrue(result); 
        assertTrue(testTrainee.isActive()); 

    }

    @Test
    void activateDeactivateTrainee_ShouldFail_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = traineeService.activateDeactivateTrainee(username, false);

        assertFalse(result);

        verify(traineeDAO, never()).save(any(Trainee.class));
    }

    @Test
    void updateTraineeProfile_ShouldSucceedAndReturnUpdatedTrainee() {
        String username = "test.user";
        String updatedFirstName = "UpdatedJohn";
        String updatedLastName = "UpdatedDoe";
        LocalDate updatedDob = LocalDate.of(1992, 2, 2);
        String updatedAddress = "456 Updated St";
        boolean updatedIsActive = false;

        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> resultOpt = traineeService.updateTraineeProfile(username, updatedFirstName, updatedLastName, updatedDob, updatedAddress, updatedIsActive);

        assertTrue(resultOpt.isPresent());
        Trainee updatedTrainee = resultOpt.get(); 
        assertEquals(updatedFirstName, updatedTrainee.getFirstName());
        assertEquals(updatedLastName, updatedTrainee.getLastName());
        assertEquals(updatedDob, updatedTrainee.getDateOfBirth());
        assertEquals(updatedAddress, updatedTrainee.getAddress());
        assertEquals(updatedIsActive, updatedTrainee.isActive());

    }

    @Test
    void updateTraineeProfile_ShouldReturnEmpty_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        Optional<Trainee> resultOpt = traineeService.updateTraineeProfile(username, "New", "Name", null, null, true);

        assertTrue(resultOpt.isEmpty());

        verify(traineeDAO, never()).save(any(Trainee.class));
    }

    @Test
    void deleteTraineeProfileByUsername_ShouldSucceed_WhenTraineeExists() {
        String username = "test.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeDAO).delete(testTrainee);

        boolean result = traineeService.deleteTraineeProfileByUsername(username);

        assertTrue(result);

        verify(traineeDAO, times(1)).delete(testTrainee);
    }

    @Test
    void deleteTraineeProfileByUsername_ShouldFail_WhenTraineeNotFound() {
        String username = "nonexistent.user";
        when(traineeDAO.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = traineeService.deleteTraineeProfileByUsername(username);

        assertFalse(result);

        verify(traineeDAO, never()).delete(any(Trainee.class));
    }
    
    @Test
    void createTraineeProfile_ShouldSucceed() {
        when(userService.generateUsername(anyString(), anyString())).thenReturn("new.user");
        when(userService.generateRandomPassword()).thenReturn("randomPass123");

        Trainee newTrainee = traineeService.createTraineeProfile("New", "User", null, "Address");

        assertNotNull(newTrainee);
        assertEquals("new.user", newTrainee.getUsername());
        assertEquals("randomPass123", newTrainee.getPassword());
        assertTrue(newTrainee.isActive());
        
        verify(traineeDAO, times(1)).save(any(Trainee.class));
    }
    
    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatus() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));
        
        boolean result = traineeService.activateDeactivateTrainee("test.user", false);
        
        assertTrue(result);
        assertFalse(testTrainee.isActive());
    }
}