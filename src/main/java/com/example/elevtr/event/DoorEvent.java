package com.example.elevtr.event;

import lombok.Getter;
import com.example.elevtr.model.ElevatrState;
import org.springframework.context.ApplicationEvent;;

@Getter
public class DoorEvent extends ApplicationEvent {

    private final int elevatorId;
    private final int floor;
    private final ElevatrState state;

    public DoorEvent(Object source, int elevatorId, int floor, ElevatrState state) {
        super(source);
        this.elevatorId = elevatorId;
        this.floor = floor;
        this.state = state;
    }
    
}
