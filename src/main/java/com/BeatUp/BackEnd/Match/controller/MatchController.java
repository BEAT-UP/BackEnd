package com.BeatUp.BackEnd.Match.controller;


import com.BeatUp.BackEnd.RideRequest.dto.request.CreateRideRequest;
import com.BeatUp.BackEnd.RideRequest.dto.response.RideRequestResponse;
import com.BeatUp.BackEnd.Match.service.MatchService;
import com.BeatUp.BackEnd.common.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("/ride-requests")
    public ResponseEntity<RideRequestResponse> createRideRequest(
            @Valid @RequestBody CreateRideRequest request
            ){

        UUID userId = SecurityUtil.getCurrentUserId();
        RideRequestResponse response = matchService.createRideRequest(userId, request);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/ride-requests/{id}")
    public ResponseEntity<RideRequestResponse> getRideRequest(@PathVariable UUID id){
        return matchService.getRideRequest(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
