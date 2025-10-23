package com.gym.crm.controller;

import com.gym.crm.dto.auth.CredentialsDto;
import com.gym.crm.dto.trainee.TraineeProfileResponseDto;
import com.gym.crm.dto.trainee.TraineeRegistrationRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainingResponseDto;
import com.gym.crm.dto.trainee.TrainerInfoDto;
import com.gym.crm.dto.trainee.UpdateTraineeProfileRequestDto;
import com.gym.crm.dto.trainee.UpdateTraineeTrainersRequestDto;
import com.gym.crm.exception.EntityNotFoundException; // Make sure this is imported
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trainee Controller", description = "Endpoints for managing trainee profiles and activities")
public class TraineeController {

    private final TraineeService traineeService;
    private final TraineeMapper traineeMapper;

    public TraineeController(TraineeService traineeService, TraineeMapper traineeMapper) {
        this.traineeService = traineeService;
        this.traineeMapper = traineeMapper;
    }

    @GetMapping
    @Operation(summary = "Get a list of all trainees")
    public ResponseEntity<List<TraineeProfileResponseDto>> getAllTrainees() {
        List<Trainee> trainees = traineeService.findAllTrainees();
        List<TraineeProfileResponseDto> responseDtos = trainees.stream()
                .map(traineeMapper::toTraineeProfileResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile and returns their generated username and password.")
    public ResponseEntity<CredentialsDto> registerTrainee(@Valid @RequestBody TraineeRegistrationRequestDto requestDto) {
        // --- FIX: Service now returns CredentialsDto directly ---
        CredentialsDto credentials = traineeService.createTraineeProfile(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getDateOfBirth(),
                requestDto.getAddress());
        // --- END FIX ---

        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(@PathVariable String username) {
        // Use EntityNotFoundException for cleaner handling (optional but good practice)
        Trainee trainee = traineeService.selectTraineeProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return ResponseEntity.ok(traineeMapper.toTraineeProfileResponseDto(trainee));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponseDto> updateTraineeProfile(@PathVariable String username, @Valid @RequestBody UpdateTraineeProfileRequestDto requestDto) {
        Trainee updatedTrainee = traineeService.updateTraineeProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        requestDto.getDateOfBirth(), requestDto.getAddress(), requestDto.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return ResponseEntity.ok(traineeMapper.toTraineeProfileResponseDto(updatedTrainee));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        boolean deleted = traineeService.deleteTraineeProfileByUsername(username);
        if (!deleted) {
             throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return ResponseEntity.ok().build();

    }

    @GetMapping("/{username}/trainers/unassigned")
    @Operation(summary = "Get unassigned trainers for a trainee") // Corrected summary
    public ResponseEntity<List<TrainerInfoDto>> getUnassignedTrainers(@PathVariable String username) {
        // Ensure trainee exists first
         traineeService.selectTraineeProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<Trainer> trainers = traineeService.getUnassignedTrainersForTrainee(username);
        List<TrainerInfoDto> response = trainers.stream().map(t -> {
            TrainerInfoDto dto = new TrainerInfoDto();
            dto.setUsername(t.getUsername());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            // Add null check for specialization, just in case
            if (t.getSpecialization() != null) {
                dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update Trainee's Trainer List")
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
    @Operation(summary = "Get Trainee Trainings List")
    public ResponseEntity<List<TraineeTrainingResponseDto>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        // Ensure trainee exists first
        traineeService.selectTraineeProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<Object[]> results = traineeService.getTraineeTrainingsList(username, fromDate, toDate, trainerName, trainingType);

        // --- FIX: Ensure mapping matches the 5 fields from the corrected service query ---
        List<TraineeTrainingResponseDto> response = results.stream()
                .map(r -> new TraineeTrainingResponseDto(
                        (String)r[0],      // trainingName
                        (LocalDate)r[1],   // trainingDate
                        (String)r[2],      // trainingTypeName
                        (Integer)r[3],     // trainingDuration
                        (String)r[4]       // trainerUsername
                ))
                .collect(Collectors.toList());
        // --- END FIX ---
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainee")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @PathVariable String username, @RequestParam boolean isActive) {
        boolean updated = traineeService.activateDeactivateTrainee(username, isActive);
         if (!updated) {
             throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return ResponseEntity.ok().build();

    }
}