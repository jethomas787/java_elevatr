package com.example.elevtr.model;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class FloorRequest {

    int floor;
    LocalDateTime requestedDateTime;

    public static FloorRequest of(int floor) {
        return FloorRequest.builder()
                .floor(floor)
                .requestedDateTime(LocalDateTime.now())
                .build();
    }
    
}
