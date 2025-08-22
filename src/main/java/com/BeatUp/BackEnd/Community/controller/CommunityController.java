package com.BeatUp.BackEnd.Community.controller;


import com.BeatUp.BackEnd.Community.Post.dto.request.CreatePostRequest;
import com.BeatUp.BackEnd.Community.Post.dto.response.PostResponse;
import com.BeatUp.BackEnd.Community.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request){
        UUID authorId = getCurrentUserId();
        PostResponse response = communityService.createPost(authorId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/posts")
    public List<PostResponse> getPosts(
            @RequestParam(required = false) UUID concertId,
            @RequestParam(required = false) String query
    ){
        return communityService.getPosts(concertId, query);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable UUID id){
        return communityService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UUID getCurrentUserId(){
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
