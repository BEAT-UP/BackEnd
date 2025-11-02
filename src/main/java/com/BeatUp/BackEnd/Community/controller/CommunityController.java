package com.BeatUp.BackEnd.Community.controller;

import com.BeatUp.BackEnd.Community.Post.dto.request.CreatePostRequest;
import com.BeatUp.BackEnd.Community.Post.dto.response.PostResponse;
import com.BeatUp.BackEnd.Community.service.CommunityService;
import com.BeatUp.BackEnd.common.dto.ApiResponse;
import com.BeatUp.BackEnd.common.dto.PageResponse;
import com.BeatUp.BackEnd.common.util.PageableUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    /**
     * Post 엔티티에서 허용할 정렬 필드들
     */
    private static final Set<String> ALLOWED_POST_SORT_FIELDS = Set.of(
            "createdAt",
            "title",
            "updatedAt"
    );

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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy
    ){
        // PageableUtil로 Pageable 생성 및 검증
        Pageable pageable = PageableUtil.createPageable(
                page,
                size,
                sortBy,
                "DESC",
                ALLOWED_POST_SORT_FIELDS
        );

        // 실제 페이징 로직 구현
        Page<PostResponse> postPage = communityService.searchPostsWithPaging(
                concertId,
                query,
                "ACTIVE",
                pageable
        );

        PageResponse<PostResponse> pageResponse = PageResponse.of(
                postPage.getContent(),
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements()
        );

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
