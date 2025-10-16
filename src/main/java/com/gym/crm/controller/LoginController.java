package com.gym.crm.controller;

import com.gym.crm.dto.LoginRequestDto;
import com.gym.crm.dto.UpdatePasswordRequestDto;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Login Controller", description = "Endpoints for managing Authentication")
public class LoginController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public LoginController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @GetMapping("/login")
    @Operation(summary = "User login for Trainee or Trainer")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        boolean traineeAuth = traineeService.checkTraineeCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        boolean trainerAuth = trainerService.checkTrainerCredentials(loginRequest.getUsername(), loginRequest.getPassword());

        if (traineeAuth || trainerAuth) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(401).build(); 
        }
    }

    @PutMapping("/change-password/{username}")
    @Operation(summary = "Change user password for Trainee or Trainer")
    public ResponseEntity<Void> changePassword(@PathVariable String username, @Valid @RequestBody UpdatePasswordRequestDto requestDto) {
        boolean traineePassChanged = traineeService.changeTraineePassword(username, requestDto.getOldPassword(), requestDto.getNewPassword());
        
        if (!traineePassChanged) {
            boolean trainerPassChanged = trainerService.changeTrainerPassword(username, requestDto.getOldPassword(), requestDto.getNewPassword());
            if (!trainerPassChanged) {
                return ResponseEntity.status(401).build(); // 401 Unauthorized
            }
        }
        
        return ResponseEntity.ok().build();
    }
}