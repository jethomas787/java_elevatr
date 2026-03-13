package com.example.elevtr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "elevtr")
public class ElevtrConfig {
    private int totalFloors = 10;
    private int totalElevators = 2;
    private long doorOpenDurationMs = 2000; 
    private long movementSpeedMs = 1000;
    private int directionChangePenalty = 3;
    private int busyElevatorPenalty = 2;
}
