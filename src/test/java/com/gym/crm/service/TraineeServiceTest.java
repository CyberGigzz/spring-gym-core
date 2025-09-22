package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class TraineeServiceTest {

    @Mock 
    private TraineeDAO traineeDAO;

    @Mock 
    private TrainerDAO trainerDAO;

    private TraineeService traineeService; 

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService();
        traineeService.setTraineeDAO(traineeDAO);
        traineeService.setTrainerDAO(trainerDAO);
    }

    @Test
    void createTraineeProfile_ShouldGenerateUsernameAndPassword_WhenUsernameIsUnique() {
        String firstName = "Peter";
        String lastName = "Jones";
        when(traineeDAO.findAll()).thenReturn(Collections.emptyList());
        when(trainerDAO.findAll()).thenReturn(Collections.emptyList());

        Trainee newTrainee = traineeService.createTraineeProfile(firstName, lastName, LocalDate.now(), "Address");

        assertNotNull(newTrainee);
        assertEquals("peter.jones", newTrainee.getUsername());
        assertNotNull(newTrainee.getPassword());
        assertEquals(10, newTrainee.getPassword().length());

        verify(traineeDAO, times(1)).save(any(Trainee.class));
    }

    @Test
    void createTraineeProfile_ShouldAppendSerial_WhenUsernameExists() {
        String firstName = "John";
        String lastName = "Doe";

        Trainee existingTrainee = new Trainee();
        existingTrainee.setUsername("john.doe");

        when(traineeDAO.findAll()).thenReturn(Collections.singletonList(existingTrainee));
        when(trainerDAO.findAll()).thenReturn(Collections.emptyList());

        Trainee newTrainee = traineeService.createTraineeProfile(firstName, lastName, LocalDate.now(), "Address");

        assertEquals("john.doe1", newTrainee.getUsername());

        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDAO).save(traineeCaptor.capture());

        Trainee savedTrainee = traineeCaptor.getValue();
        assertEquals("john.doe1", savedTrainee.getUsername());
    }

    @Test
    void selectTraineeProfile_ShouldReturnTrainee_WhenFound() {
        Trainee fakeTrainee = new Trainee();
        fakeTrainee.setId(1L);
        fakeTrainee.setFirstName("Found");
        fakeTrainee.setLastName("User");

        when(traineeDAO.findById(1L)).thenReturn(Optional.of(fakeTrainee));

        Optional<Trainee> result = traineeService.selectTraineeProfile(1L);

        assertTrue(result.isPresent());
        assertEquals("Found", result.get().getFirstName());

        verify(traineeDAO, times(1)).findById(1L);
    }

    @Test
    void selectTraineeProfile_ShouldReturnEmpty_WhenNotFound() {
        when(traineeDAO.findById(99L)).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.selectTraineeProfile(99L);

        assertFalse(result.isPresent());

        verify(traineeDAO, times(1)).findById(99L);
    }
}