package com.gym.crm.controller;

import com.gym.crm.dto.training.AddTrainingRequestDto;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training Controller", description = "Endpoints for managing trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public TrainingController(TrainingService trainingService, TrainingTypeService trainingTypeService) {
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    @PostMapping
    @Operation(summary = "Add a new training (Task 14)")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequestDto requestDto) {
        
        // 1. Get the TrainingType entity from the ID
        TrainingType trainingType = trainingTypeService.findById(requestDto.getTrainingTypeId())
                .orElseThrow(() -> new RuntimeException("TrainingType not found")); // We will fix this with proper error handling

        // 2. Call your existing service method
        Training newTraining = trainingService.addTraining(
                requestDto.getTraineeUsername(),
                requestDto.getTrainerUsername(),
                requestDto.getTrainingName(),
                trainingType, // Pass the full entity
                requestDto.getTrainingDate(),
                requestDto.getTrainingDuration()
        );

        if (newTraining != null) {
            return ResponseEntity.ok().build();
        } else {
            // This happens if trainee or trainer wasn't found in your service
            return ResponseEntity.badRequest().build();
        }
    }
}