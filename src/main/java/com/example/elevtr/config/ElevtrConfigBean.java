package com.example.elevtr.config;


import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.elevtr.service.FloorRequestService;
import com.example.elevtr.service.ElevtrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElevtrConfigBean {
    
    private final ElevtrConfig config;
    private final FloorRequestService floorRequestService;
    private final ApplicationEventPublisher eventPublisher; 

    @Bean
    public ElevtrService elevtrService() {
        return new ElevtrService(1, config, floorRequestService, eventPublisher);
    }
    @Bean
    public ElevtrService elevtrService2() {
        return new ElevtrService(2, config, floorRequestService, eventPublisher);
    }                               

    @Bean
    public CommandLineRunner startElevtr(List<ElevtrService> elevtrs) {
        return args -> { 
            log.info("Starting Elevtr Simulation with {} elevators and {} floors", elevtrs.size());

            elevtrs.forEach(elevtr -> {
                Thread.ofVirtual()
                .name("elevtr-thread-" + elevtr.getElevatorId())
                .start(elevtr);
            });
            log.info("Elevtr Simulation started successfully");
            log.info("You can send floor requests to http://localhost:8080/request/{{floor}}");
        };
    }
}
