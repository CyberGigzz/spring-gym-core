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
    public ResponseEntity<CredentialsDto> registerTrainer(@Valid @RequestBody TrainerRegistrationRequestDto requestDto) {

        TrainingType specialization = trainingTypeService.findById(requestDto.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + requestDto.getSpecializationId()));

        CredentialsDto credentials = trainerService.createTrainerProfile(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                specialization);

        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(@PathVariable String username) {
        Trainer trainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        return ResponseEntity.ok(trainerMapper.toTrainerProfileResponseDto(trainer));
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainerProfile(
            @PathVariable String username, @Valid @RequestBody UpdateTrainerProfileRequestDto requestDto) {

        Trainer existingTrainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        Trainer updatedTrainer = trainerService.updateTrainerProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        existingTrainer.getSpecialization(), 
                        requestDto.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found during update for username: " + username)); 

        return ResponseEntity.ok(trainerMapper.toTrainerProfileResponseDto(updatedTrainer));
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName) { 

        trainerService.selectTrainerProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        List<Object[]> results = trainerService.getTrainerTrainingsList(username, fromDate, toDate, traineeName);

        List<TrainerTrainingResponseDto> response = results.stream()
                .map(r -> new TrainerTrainingResponseDto(
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
    public ResponseEntity<Void> activateDeactivateTrainer(
            @PathVariable String username, @RequestParam boolean isActive) {
        boolean updated = trainerService.activateDeactivateTrainer(username, isActive);
         if (!updated) {
             throw new EntityNotFoundException("Trainer not found with username: " + username);
        }
        return ResponseEntity.ok().build();
    }
}