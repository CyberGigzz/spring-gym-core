package com.gym.crm.controller;

import com.gym.crm.dto.auth.CredentialsDto;
import com.gym.crm.dto.trainee.TraineeProfileResponseDto;
import com.gym.crm.dto.trainee.TraineeRegistrationRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainingResponseDto;
import com.gym.crm.dto.trainee.TrainerInfoDto;
import com.gym.crm.dto.trainee.UpdateTraineeProfileRequestDto;
import com.gym.crm.dto.trainee.UpdateTraineeTrainersRequestDto;
import com.gym.crm.exception.EntityNotFoundException; 
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.service.TraineeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TraineeMapper traineeMapper;

    public TraineeController(TraineeService traineeService, TraineeMapper traineeMapper) {
        this.traineeService = traineeService;
        this.traineeMapper = traineeMapper;
    }

    @GetMapping
    public ResponseEntity<List<TraineeProfileResponseDto>> getAllTrainees() {
        List<Trainee> trainees = traineeService.findAllTrainees();
        List<TraineeProfileResponseDto> responseDtos = trainees.stream()
                .map(traineeMapper::toTraineeProfileResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping("/register")
    public ResponseEntity<CredentialsDto> registerTrainee(@Valid @RequestBody TraineeRegistrationRequestDto requestDto) {
        CredentialsDto credentials = traineeService.createTraineeProfile(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getDateOfBirth(),
                requestDto.getAddress());

        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(@PathVariable String username) {
        Trainee trainee = traineeService.selectTraineeProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return ResponseEntity.ok(traineeMapper.toTraineeProfileResponseDto(trainee));
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> updateTraineeProfile(@PathVariable String username, @Valid @RequestBody UpdateTraineeProfileRequestDto requestDto) {
        Trainee updatedTrainee = traineeService.updateTraineeProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        requestDto.getDateOfBirth(), requestDto.getAddress(), requestDto.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return ResponseEntity.ok(traineeMapper.toTraineeProfileResponseDto(updatedTrainee));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        boolean deleted = traineeService.deleteTraineeProfileByUsername(username);
        if (!deleted) {
             throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return ResponseEntity.ok().build();

    }

    @GetMapping("/{username}/trainers/unassigned")
    public ResponseEntity<List<TrainerInfoDto>> getUnassignedTrainers(@PathVariable String username) {
         traineeService.selectTraineeProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<Trainer> trainers = traineeService.getUnassignedTrainersForTrainee(username);
        List<TrainerInfoDto> response = trainers.stream().map(t -> {
            TrainerInfoDto dto = new TrainerInfoDto();
            dto.setUsername(t.getUsername());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            if (t.getSpecialization() != null) {
                dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerInfoDto>> updateTraineeTrainers(
            @PathVariable String username, @Valid @RequestBody UpdateTraineeTrainersRequestDto requestDto) {
        List<Trainer> updatedTrainers = traineeService.updateTraineeTrainersList(username, requestDto.getTrainerUsernames())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<TrainerInfoDto> response = updatedTrainers.stream().map(t -> {
            TrainerInfoDto dto = new TrainerInfoDto();
            dto.setUsername(t.getUsername());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            if (t.getSpecialization() != null) {
                dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponseDto>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        traineeService.selectTraineeProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<Object[]> results = traineeService.getTraineeTrainingsList(username, fromDate, toDate, trainerName, trainingType);

        List<TraineeTrainingResponseDto> response = results.stream()
                .map(r -> new TraineeTrainingResponseDto(
                        (String)r[0],      
                        (LocalDate)r[1],   
                        (String)r[2],      
                        (Integer)r[3],     
                        (String)r[4]       
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @PathVariable String username, @RequestParam boolean isActive) {
        boolean updated = traineeService.activateDeactivateTrainee(username, isActive);
         if (!updated) {
             throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return ResponseEntity.ok().build();

    }
}