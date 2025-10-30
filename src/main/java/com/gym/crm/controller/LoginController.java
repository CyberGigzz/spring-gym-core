package com.gym.crm.controller;

import com.gym.crm.dto.auth.LoginRequestDto;
import com.gym.crm.dto.auth.UpdatePasswordRequestDto;
import com.gym.crm.exception.AuthenticationFailedException;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user login and password changes (Tasks 3 & 4)")
public class LoginController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public LoginController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login (Task 3)", description = "Authenticates a Trainee or Trainer based on username and password.")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        boolean traineeAuth = traineeService.checkTraineeCredentials(loginRequest.getUsername(), loginRequest.getPassword());
        boolean trainerAuth = trainerService.checkTrainerCredentials(loginRequest.getUsername(), loginRequest.getPassword());

        if (traineeAuth || trainerAuth) {
            return ResponseEntity.ok().build();
        } else {
            throw new AuthenticationFailedException("Invalid username or password");
        }
    }

    @PutMapping("/change-password/{username}")
    @Operation(summary = "Change user password (Task 4)", description = "Changes the password for a Trainee or Trainer after validating the old password.")
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