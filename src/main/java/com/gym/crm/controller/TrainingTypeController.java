package com.gym.crm.controller;

import com.gym.crm.dto.training.TrainingTypeDto;
import com.gym.crm.mapper.TrainingTypeMapper;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Type Controller", description = "Endpoint for getting training types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainingTypeController(TrainingTypeService trainingTypeService, TrainingTypeMapper trainingTypeMapper) {
        this.trainingTypeService = trainingTypeService;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    @GetMapping
    @Operation(summary = "Get all training types (Task 17)")
    public ResponseEntity<List<TrainingTypeDto>> getAllTrainingTypes() {
        List<TrainingType> types = trainingTypeService.findAll();
        
        List<TrainingTypeDto> dtos = types.stream()
                .map(trainingTypeMapper::toDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(dtos);
    }
}