package com.gym.crm.controller;

import com.gym.crm.dto.*;
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

    @PostMapping("/register")
    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile and returns their generated username and password.")
    public ResponseEntity<CredentialsDto> registerTrainee(@Valid @RequestBody TraineeRegistrationRequestDto requestDto) {
        Trainee newTrainee = traineeService.createTraineeProfile(
                requestDto.getFirstName(), requestDto.getLastName(), requestDto.getDateOfBirth(), requestDto.getAddress());
        CredentialsDto credentials = new CredentialsDto();
        credentials.setUsername(newTrainee.getUsername());
        credentials.setPassword(newTrainee.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(@PathVariable String username) {
        return traineeService.selectTraineeProfileByUsername(username)
                .map(traineeMapper::toTraineeProfileResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponseDto> updateTraineeProfile(@PathVariable String username, @Valid @RequestBody UpdateTraineeProfileRequestDto requestDto) {
        return traineeService.updateTraineeProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        requestDto.getDateOfBirth(), requestDto.getAddress(), requestDto.isActive())
                .map(traineeMapper::toTraineeProfileResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        return traineeService.deleteTraineeProfileByUsername(username)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{username}/trainers/unassigned")
    @Operation(summary = "Activate or deactivate a trainee")
    public ResponseEntity<List<TrainerInfoDto>> getUnassignedTrainers(@PathVariable String username) {
        List<Trainer> trainers = traineeService.getUnassignedTrainersForTrainee(username);
        List<TrainerInfoDto> response = trainers.stream().map(t -> {
            TrainerInfoDto dto = new TrainerInfoDto();
            dto.setUsername(t.getUsername());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update Trainee's Trainer List")
    public ResponseEntity<List<TrainerInfoDto>> updateTraineeTrainers(
            @PathVariable String username, @Valid @RequestBody UpdateTraineeTrainersRequestDto requestDto) {
        return traineeService.updateTraineeTrainersList(username, requestDto.getTrainerUsernames())
                .map(trainers -> trainers.stream().map(t -> {
                    TrainerInfoDto dto = new TrainerInfoDto();
                    dto.setUsername(t.getUsername());
                    dto.setFirstName(t.getFirstName());
                    dto.setLastName(t.getLastName());
                    dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
                    return dto;
                }).collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get Trainee Trainings List")
    public ResponseEntity<List<TraineeTrainingResponseDto>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        List<Object[]> results = traineeService.getTraineeTrainingsList(username, fromDate, toDate, trainerName, trainingType);
        List<TraineeTrainingResponseDto> response = results.stream()
                .map(r -> new TraineeTrainingResponseDto((String)r[0], (LocalDate)r[1], (String)r[2], (Integer)r[3], (String)r[4]))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainee")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @PathVariable String username, @RequestParam boolean isActive) {
        return traineeService.activateDeactivateTrainee(username, isActive)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}