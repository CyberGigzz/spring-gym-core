package com.gym.crm.controller;

import com.gym.crm.dto.auth.CredentialsDto;
import com.gym.crm.dto.trainer.TrainerProfileResponseDto;
import com.gym.crm.dto.trainer.TrainerRegistrationRequestDto;
import com.gym.crm.dto.trainer.TrainerTrainingResponseDto;
import com.gym.crm.dto.trainer.UpdateTrainerProfileRequestDto;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingTypeService;
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
    private final TrainingTypeService trainingTypeService;
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

        TrainingType specialization = trainingTypeService.findById(requestDto.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + requestDto.getSpecializationId()));

        // --- FIX: Service now returns CredentialsDto directly ---
        CredentialsDto credentials = trainerService.createTrainerProfile(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                specialization);
        // --- END FIX ---

        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username (Task 8)")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(@PathVariable String username) {
        Trainer trainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        return ResponseEntity.ok(trainerMapper.toTrainerProfileResponseDto(trainer));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile (Task 9)")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainerProfile(
            @PathVariable String username, @Valid @RequestBody UpdateTrainerProfileRequestDto requestDto) {

        // Get existing trainer to preserve specialization (Task 9 - read only)
        Trainer existingTrainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        Trainer updatedTrainer = trainerService.updateTrainerProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        existingTrainer.getSpecialization(), // Use existing specialization
                        requestDto.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found during update for username: " + username)); // Should not happen if first find worked

        return ResponseEntity.ok(trainerMapper.toTrainerProfileResponseDto(updatedTrainer));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get Trainer Trainings List (Task 13)")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName) { // Parameter name matches service

        // Ensure trainer exists first
        trainerService.selectTrainerProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        List<Object[]> results = trainerService.getTrainerTrainingsList(username, fromDate, toDate, traineeName);

        // --- FIX: Ensure mapping matches the 5 fields from the corrected service query ---
        List<TrainerTrainingResponseDto> response = results.stream()
                .map(r -> new TrainerTrainingResponseDto(
                        (String)r[0],      // trainingName
                        (LocalDate)r[1],   // trainingDate
                        (String)r[2],      // trainingTypeName
                        (Integer)r[3],     // trainingDuration
                        (String)r[4]       // traineeUsername
                ))
                .collect(Collectors.toList());
        // --- END FIX ---

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainer (Task 16)")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @PathVariable String username, @RequestParam boolean isActive) {
        boolean updated = trainerService.activateDeactivateTrainer(username, isActive);
         if (!updated) {
             throw new EntityNotFoundException("Trainer not found with username: " + username);
        }
        return ResponseEntity.ok().build();
    }
}