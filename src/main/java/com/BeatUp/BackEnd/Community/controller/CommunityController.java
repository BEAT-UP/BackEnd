package com.BeatUp.BackEnd.Community.controller;

import com.BeatUp.BackEnd.Community.Post.dto.request.CreatePostRequest;
import com.BeatUp.BackEnd.Community.Post.dto.response.PostResponse;
import com.BeatUp.BackEnd.Community.service.CommunityService;
import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    // 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody CreatePostRequest request){
        PostResponse response = communityService.createPost(request);
        ApiResponse<PostResponse> apiResponse = ApiResponse.success(response, "게시글 생성 성공");
        return ResponseEntity.status(201).body(apiResponse);
    }

    // 게시글 조회
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getPosts(
            @RequestParam(required = false) UUID concertId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        // TODO: 실제 페이징 로직 구현 필요
        PageResponse<PostResponse> pageResponse = PageResponse.empty();
        ApiResponse<PageResponse<PostResponse>> apiResponse = ApiResponse.success(pageResponse, "게시글 목록 조회 성공");
        return ResponseEntity.ok(apiResponse);
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable UUID postId){
       PostResponse response = communityService.getPostById(postId);
       ApiResponse<PostResponse> apiResponse = ApiResponse.success(response, "게시글 상세 조회 성공");
       return ResponseEntity.ok(apiResponse);
    }


}
