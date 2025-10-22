package com.gym.crm.controller;

import com.gym.crm.dto.*;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingTypeService; // We will need this
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
@RequestMapping("/api/trainers")
@Tag(name = "Trainer Controller", description = "Endpoints for managing trainer profiles and activities")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService; // For Task 2
    private final TrainerMapper trainerMapper;

    public TrainerController(TrainerService trainerService, 
                             TrainingTypeService trainingTypeService, 
                             TrainerMapper trainerMapper) {
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
        this.trainerMapper = trainerMapper;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new trainer (Task 2)")
    public ResponseEntity<CredentialsDto> registerTrainer(@Valid @RequestBody TrainerRegistrationRequestDto requestDto) {
        
        // We need to fetch the TrainingType entity from the ID
        TrainingType specialization = trainingTypeService.findById(requestDto.getSpecializationId())
                .orElseThrow(() -> new RuntimeException("TrainingType not found")); // We'll fix this with GlobalExceptionHandler

        Trainer newTrainer = trainerService.createTrainerProfile(
                requestDto.getFirstName(), requestDto.getLastName(), specialization);
        
        CredentialsDto credentials = new CredentialsDto();
        credentials.setUsername(newTrainer.getUsername());
        credentials.setPassword(newTrainer.getPassword());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username (Task 8)")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(@PathVariable String username) {
        return trainerService.selectTrainerProfileByUsername(username)
                .map(trainerMapper::toTrainerProfileResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile (Task 9)")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainerProfile(
            @PathVariable String username, @Valid @RequestBody UpdateTrainerProfileRequestDto requestDto) {
        
        // Task 9 says specialization is read-only, so we get the existing one
        Trainer existingTrainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        return trainerService.updateTrainerProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        existingTrainer.getSpecialization(), requestDto.isActive())
                .map(trainerMapper::toTrainerProfileResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get Trainer Trainings List (Task 13)")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName) {

        List<Object[]> results = trainerService.getTrainerTrainingsList(username, fromDate, toDate, traineeName);
        
        // This mapping matches the new DTO
        List<TrainerTrainingResponseDto> response = results.stream()
                .map(r -> new TrainerTrainingResponseDto((String)r[0], (LocalDate)r[1], (String)r[2], null, (String)r[3])) // We need to fix the service query
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainer (Task 16)")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @PathVariable String username, @RequestParam boolean isActive) {
        return trainerService.activateDeactivateTrainer(username, isActive)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}