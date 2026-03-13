package com.example.elevtr.service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FloorRequestService {
    
    private final Set<Integer> pendingRequests  = ConcurrentHashMap.newKeySet();

    public void addRequest(int floor) {
        boolean added = pendingRequests.add(floor);
        if (added) {
            log.info("Added floor request: {}", floor);
        } else {
            log.info("Floor {} is already requested.", floor);
        }
    }  
    
    public void removeRequest(int floor) {
        boolean removed = pendingRequests.remove(floor);
        if (removed) {
             log.debug("✅ Floor {} request cleared. Remaining: {}",
                    floor, pendingRequests);
        }
    } 
    
    public boolean hasPendingRequests(int floor) {
        return pendingRequests.contains(floor);
    }   

    public Set<Integer> getAllpendingRequests() {
        return Collections.unmodifiableSet(pendingRequests);
    }   

    public boolean isEmpty() {
        return pendingRequests.isEmpty();
    }   
}
