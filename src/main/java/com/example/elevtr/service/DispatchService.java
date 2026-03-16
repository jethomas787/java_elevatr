package com.example.elevtr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.elevtr.config.ElevtrConfig;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class DispatchService  {

    private final List<ElevtrService>  elevtrs;
    private final FloorRequestService floorRequestService;
    private final ElevtrConfig config;

    public DispatchService(List<ElevtrService> elevtrs, FloorRequestService floorRequestService, ElevtrConfig config, FloorRequestService floorRequestService_1) {
        this.floorRequestService = floorRequestService;
        this.elevtrs = elevtrs;
        this.config = config;
    }

    /**
     * @param requestedFloor
     */
    public synchronized void dispatch(int requestedFloor) {
        log.info("Dispatching request for floor {}", requestedFloor);
        if(requestedFloor < 0 || requestedFloor >= config.getTotalFloors()) {
            log.warn("Invalid floor request: {}. Ignoring.", requestedFloor);
            return;
        }
        
        floorRequestService.addRequest(requestedFloor);

        ElevtrService bestElevator = findBestElevator(requestedFloor);
       
        if (bestElevator == null) {
            log.warn("No available elevators to dispatch for floor {}", requestedFloor);
            return;
        }
  
         log.info("Dispatching Elevator-{} to floor {}", bestElevator.getElevatorId(), requestedFloor);
            
         bestElevator.assignFloor(requestedFloor);
    }

    private ElevtrService findBestElevator(int requestedFloor) {
        ElevtrService bestElevator = null;
        int bestScore = Integer.MAX_VALUE;

        for (ElevtrService elevtr : elevtrs) {
           if(!elevtr.getState().isAcceptingRequests()) {
                log.debug("Elevator {} is not accepting requests (state: {}). Skipping.", elevtr.getElevatorId(), elevtr.getState());
                continue;
            }
           
            int score = elevtr.calculateScore(requestedFloor);
            log.debug("Elevator {} score for floor {}: {}", elevtr.getElevatorId(), requestedFloor, score);
            if (score < bestScore) {
                bestScore = score;
                bestElevator = elevtr;
            }
        }

        return bestElevator;
    }
   



    
}
