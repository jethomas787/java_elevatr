package com.example.elevtr.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ElevatrEventListener {

    @EventListener
    public void handleDoorEvent(DoorEvent event) {
        switch (event.getState()) {
            case DOOR_OPEN -> log.info(
                " [Elevator-{}] Floor {}: Doors opened.", 
                        event.getElevatorId(), 
                        event.getFloor()
                    );
            
             case DOOR_CLOSE -> log.info(
                " [Elevator-{}] Floor {}: Doors closed.", 
                        event.getElevatorId(), 
                        event.getFloor()
                    );   
            default -> log.warn(
                "[Elevator-{}] Unexpected door event type: {}", 
                event.getElevatorId(), 
                event.getState()
            );
            
        }
        
        log.info("Door Event: Elevator {} at Floor {} is now {}", 
                 event.getElevatorId(), event.getFloor(), event.getState());
    }
    
}   