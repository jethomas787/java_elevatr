package com.example.elevtr.service;

import com.example.elevtr.config.ElevtrConfig;
import com.example.elevtr.event.DoorEvent;
import com.example.elevtr.model.Direction;
import com.example.elevtr.model.ElevatrState;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Slf4j
@Service
public class ElevtrService implements Runnable{

    private int elevatorId;
    private final ElevtrConfig config;
    private final FloorRequestService floorRequestService;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicInteger currentFloor = new AtomicInteger(0);
    private volatile ElevatrState state =  ElevatrState.IDLE;
    private volatile Direction direction = Direction.UP;
    private volatile int targetFloor = -1;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition hasWork = lock.newCondition();


    private Direction determineDirection(int requestedFloor) {
        if (requestedFloor > currentFloor.get()) {
            return Direction.UP;
        }
        return Direction.DOWN;
    }

    private boolean isMovingToward(int requestedFloor) {
        if (direction == Direction.UP && requestedFloor > currentFloor.get()) {
            return true;
        }
        if (direction == Direction.DOWN && requestedFloor < currentFloor.get()) {
            return true;
        }
        return false;
    }


    public ElevtrService(int elevatorId, ElevtrConfig config, FloorRequestService floorRequestService, 
        ApplicationEventPublisher eventPublisher) {
        
        this.elevatorId = elevatorId;
        this.config = config;
        this.floorRequestService = floorRequestService;
        this.eventPublisher = eventPublisher;
    }

    public void run() {
        log.info("Elevator {} started at floor {}", elevatorId, currentFloor.get());
        
        while (Thread.currentThread().isInterrupted()) {
            int floorToServe;
            
            lock.lock();
            try {
                while (targetFloor == -1) {
                   log.debug("IDLE waiting for floor assignment", elevatorId);
                    setState(ElevatrState.IDLE);
                    try {
                        hasWork.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                floorToServe = targetFloor;
            }finally{
                lock.unlock();
            }
            moveToFloor(floorToServe);
        }
    }

    public void assignFloor(int floor) {
        lock.lock();
        try {
            this.targetFloor = floor;
            this.direction = determineDirection(floor);
            log.info("Elevator {} assigned to floor {}. Current floor: {}, Direction: {}",
                    elevatorId, floor, currentFloor.get(), direction);
            hasWork.signal();
        } finally {
            lock.unlock();
        }
    }

    public int calculateScore(int requestedFloor) {
        int distance = Math.abs(requestedFloor - currentFloor.get() - requestedFloor);
        
        boolean movingToward = isMovingToward(requestedFloor);

        int directionPenalty = (state == ElevatrState.MOVING && !movingToward) ? config.getDirectionChangePenalty() : 0;
        int busyPenalty = floorRequestService.hasPendingRequests(requestedFloor) ? config.getBusyElevatorPenalty() : 0;
        int score = distance + directionPenalty + busyPenalty;
        log.debug("Elevator {} score for floor {}: distance={}, directionPenalty={}, busyPenalty={}, totalScore={}",
                elevatorId, requestedFloor, distance, directionPenalty, busyPenalty);
        return score;
    }

       private void moveToFloor(int targetFloor) {
        setState(ElevatrState.MOVING);
 

        while (currentFloor.get() != targetFloor) {
            int next = currentFloor.get() + direction.step();
 
            simulateFloorTravel();          // sleep to simulate movement time
 
            currentFloor.set(next);
            log.info("🛗  [Elevator-{}] passing floor {}", elevatorId, next);
 

            if (floorRequestService.hasPendingRequests(next)
                    && next != targetFloor) {
                log.info("🛗  [Elevator-{}] intermediate stop at floor {}",
                        elevatorId, next);
                arriveAtFloor(next);
                setState(ElevatrState.MOVING);
            }
        }
 
        arriveAtFloor(targetFloor);

        lock.lock();
        try {
            this.targetFloor = -1;
        } finally {
            lock.unlock();
        }
    }

    private void arriveAtFloor(int floor) {
        setState(ElevatrState.ARRIVED);
        log.info("🛗  [Elevator-{}] ✅ arrived at floor {}", elevatorId, floor);
 
        floorRequestService.removeRequest(floor);
 
        setState(ElevatrState.DOOR_OPEN);
        eventPublisher.publishEvent(
                new DoorEvent(this, elevatorId, floor, ElevatrState.DOOR_OPEN)
        );
 
        simulateDoorOpen();
 
        setState(ElevatrState.DOOR_CLOSE);
        eventPublisher.publishEvent(
                new DoorEvent(this, elevatorId, floor, ElevatrState.DOOR_CLOSE)
        );
    }

    private void setState(ElevatrState newState) {
        log.debug("🛗  [Elevator-{}] state: {} → {}", elevatorId, this.state, newState);
        this.state = newState;
    }

    private void simulateFloorTravel() {
        try {
            Thread.sleep(config.getMovementSpeedMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
 
    private void simulateDoorOpen() {
        try {
            Thread.sleep(config.getDoorOpenDurationMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
