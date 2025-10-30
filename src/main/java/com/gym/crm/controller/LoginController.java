package com.gym.crm.controller;

import com.gym.crm.dto.auth.LoginRequestDto;
import com.gym.crm.dto.auth.UpdatePasswordRequestDto;
import com.gym.crm.exception.AuthenticationFailedException;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public LoginController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @PostMapping("/login")
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