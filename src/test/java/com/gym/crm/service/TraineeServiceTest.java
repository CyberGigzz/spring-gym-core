package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    void changeTraineePassword_ShouldSucceed_WhenCredentialsAreCorrect() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));

        boolean result = traineeService.changeTraineePassword("test.user", "oldPassword", "newPassword");

        assertTrue(result);
        assertEquals("newPassword", testTrainee.getPassword()); 
    }

    @Test
    void changeTraineePassword_ShouldFail_WhenOldPasswordIsIncorrect() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));

        boolean result = traineeService.changeTraineePassword("test.user", "wrongOldPassword", "newPassword");

        assertFalse(result);
        assertEquals("oldPassword", testTrainee.getPassword()); 
    }
    
    @Test
    void activateDeactivateTrainee_ShouldChangeActiveStatus() {
        when(traineeDAO.findByUsername("test.user")).thenReturn(Optional.of(testTrainee));
        
        boolean result = traineeService.activateDeactivateTrainee("test.user", false);
        
        assertTrue(result);
        assertFalse(testTrainee.isActive());
    }
}