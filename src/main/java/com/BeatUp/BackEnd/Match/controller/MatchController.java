package com.BeatUp.BackEnd.Match.controller;

import com.BeatUp.BackEnd.RideRequest.dto.request.CreateRideRequest;
import com.BeatUp.BackEnd.RideRequest.dto.response.RideRequestResponse;
import com.BeatUp.BackEnd.Match.service.MatchService;
import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.enums.ErrorCode;
import com.BeatUp.BackEnd.common.exception.BusinessException;
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
    public ResponseEntity<ApiResponse<RideRequestResponse>> createRideRequest(
            @Valid @RequestBody CreateRideRequest request
            ){

        UUID userId = SecurityUtil.getCurrentUserId();
        RideRequestResponse response = matchService.createRideRequest(userId, request);

        ApiResponse<RideRequestResponse> apiResponse = ApiResponse.success(response, "라이드 요청 생성 성공");
        return ResponseEntity.status(201).body(apiResponse);
    }

    @GetMapping("/ride-requests/{id}")
    public ResponseEntity<ApiResponse<RideRequestResponse>> getRideRequest(@PathVariable UUID id){
        RideRequestResponse response = matchService.getRideRequest(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "라이드 요청을 찾을 수 없습니다"));
        
        ApiResponse<RideRequestResponse> apiResponse = ApiResponse.success(response, "라이드 요청 조회 성공");
        return ResponseEntity.ok(apiResponse);
    }


}
