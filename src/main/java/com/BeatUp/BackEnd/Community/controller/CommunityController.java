package com.BeatUp.BackEnd.Community.controller;


import com.BeatUp.BackEnd.Community.Post.dto.request.CreatePostRequest;
import com.BeatUp.BackEnd.Community.Post.dto.response.PostResponse;
import com.BeatUp.BackEnd.Community.service.CommunityService;
import com.BeatUp.BackEnd.common.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.support.PageableUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    // 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request){
        PostResponse response = communityService.createPost(request);
        return ResponseEntity.status(201).body(response);
    }

    // 게시글 조회
    @GetMapping("/posts")
    public List<PostResponse> getPosts(
            @RequestParam(required = false) UUID concertId,
            @RequestParam(required = false) String query
    ){
        return null;
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable UUID postId){
       PostResponse response = communityService.getPostById(postId);
       return ResponseEntity.ok(response);
    }


}
