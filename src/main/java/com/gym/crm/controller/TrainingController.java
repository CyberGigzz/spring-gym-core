package com.gym.crm.controller;

import com.gym.crm.dto.training.AddTrainingRequestDto;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.TrainingTypeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public TrainingController(TrainingService trainingService, TrainingTypeService trainingTypeService) {
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequestDto requestDto) {
        
        TrainingType trainingType = trainingTypeService.findById(requestDto.getTrainingTypeId())
                .orElseThrow(() -> new RuntimeException("TrainingType not found")); 

        Training newTraining = trainingService.addTraining(
                requestDto.getTraineeUsername(),
                requestDto.getTrainerUsername(),
                requestDto.getTrainingName(),
                trainingType, 
                requestDto.getTrainingDate(),
                requestDto.getTrainingDuration()
        );

        if (newTraining != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}