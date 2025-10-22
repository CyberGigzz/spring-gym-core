package com.gym.crm.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrainerProfileResponseDto {
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private List<TraineeInfoDto> trainees;
}