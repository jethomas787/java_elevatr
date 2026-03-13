package com.example.elevtr.model;

public enum ElevatrState {
    
    IDLE,
    MOVING,
    ARRIVED,
    DOOR_OPEN,
    DOOR_CLOSE;

    public boolean isAcceptingRequests() {
        return this == ElevatrState.IDLE || this == ElevatrState.MOVING;
    }

    public boolean isMoving() {
        return this == ElevatrState.MOVING;
    }

}
