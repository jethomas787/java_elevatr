package com.example.elevtr.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.elevtr.config.ElevtrConfig;
import com.example.elevtr.service.DispatchService;
import com.example.elevtr.service.ElevtrService;
import com.example.elevtr.service.FloorRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;



@Slf4j
@RestController
@RequestMapping("/elevtr")
@RequiredArgsConstructor
public class SImulationController {

    private final DispatchService dispatchService;
    private final FloorRequestService floorRequestService;
    private final List<ElevtrService> elevtrServices;   
    private final ElevtrConfig config;


    @PostMapping("/request/{floor}")
    public ResponseEntity<Map<String,Object>> requestFloor(@PathVariable int floor) {
        
        log.info("Received floor request: {}", floor);
        if(floor < 0 || floor >= config.getTotalFloors()) {
           Map<String, Object> error = new LinkedHashMap<>();
           error.put("status", "error");
           error.put("message", "Invalid floor number: " + floor);
           error.put("validRange", "0 to " + (config.getTotalFloors()-1));

            return ResponseEntity.badRequest().body(error);
        }
       
        dispatchService.dispatch(floor);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "accepted");
        response.put("message", "Floor request received");
        response.put("pendingRequest", floorRequestService.getAllpendingRequests());
        return ResponseEntity.accepted().body(response);
    }   

    @GetMapping("/status")
    public ResponseEntity<Map<String,Object>> getStatus() {
        List<Map<String, Object>> elevatorsStatus = elevtrServices.stream().map(elevtr -> {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("elevatorId", elevtr.getElevatorId());
            status.put("currentFloor", elevtr.getCurrentFloor());
            status.put("state", elevtr.getState());
            status.put("direction", elevtr.getDirection());
            status.put("targetFloor", elevtr.getTargetFloor());
            return status;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("elevators", elevatorsStatus);
        response.put("pendingRequests", floorRequestService.getAllpendingRequests());
        return ResponseEntity.ok(response);
    }

    
}
