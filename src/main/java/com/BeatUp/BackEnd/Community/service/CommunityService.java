package com.BeatUp.BackEnd.Community.service;


import com.BeatUp.BackEnd.Community.Post.dto.PostSearchCondition;
import com.BeatUp.BackEnd.Community.Post.dto.request.CreatePostRequest;
import com.BeatUp.BackEnd.Community.Post.dto.response.PostResponse;
import com.BeatUp.BackEnd.Community.Post.entity.Post;
import com.BeatUp.BackEnd.Community.Post.repository.PostCustomRepository;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Community.Post.repository.PostRepository;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import com.BeatUp.BackEnd.common.exception.ResourceNotFoundException;
import com.BeatUp.BackEnd.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCustomRepository postCustomRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * 게시글 생성
     * @param request
     */
    @Transactional
    public PostResponse createPost(CreatePostRequest request){
        // SecurityUtil을 통한 사용자 ID 자동 추출
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // 1. 프로필 완성 여부 확인
        UserProfile profile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다"));

        if(!profile.isProfileCompleted()){
            throw new IllegalArgumentException("프로필을 먼저 완성해주세요");
        }

        // 2. Concert 존재 여부 확인
        if(!concertRepository.existsById(request.getConcertId())){
            throw new IllegalArgumentException("존재하지 않는 공연입니다.");
        }

        // 3. 게시글 생성(Builder 패턴)
        Post post = Post.builder()
                .authorId(currentUserId)
                .concertId(request.getConcertId())
                .title(request.getTitle())
                .content(request.getContent())
                .status("ACTIVE")
                .build();

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
    }

    /**
     * 게시글 동적 검색(QueryDSL 활용)
     * @param conertId
     * @param query
     * @param status
     */
    public List<PostResponse> searchPosts(UUID conertId, String query, String status){
        PostSearchCondition condition = PostSearchCondition.builder()
                .concertId(conertId)
                .query(query)
                .status(status != null ? status : "ACTIVE")
                .build();

        List<Post> posts = postCustomRepository.searchPosts(condition);

        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 페이징 검색(QueryDSL + Spring Data JPA 페이징)
     * @param concertId
     * @param query
     * @param status
     * @param pageable
     */
    public Page<PostResponse> searchPostsWithPaging(UUID concertId, String query, String status, Pageable pageable){
        PostSearchCondition condition = PostSearchCondition.builder()
                .concertId(concertId)
                .query(query)
                .status(status != null ? status: "ACTIVE")
                .build();

        Page<Post> postPage = postCustomRepository.searchPosts(condition, pageable);
        return postPage.map(this::mapToResponse);
    }

    // 게시글 상세 조회
    public PostResponse getPostById(UUID postId){
        Post post = postRepository.findOrThrow(postId);

        // ACTIVE 상태 확인
        if(!"ACTIVE".equals(post.getStatus())){
            throw new ResourceNotFoundException("삭제되거나 비활성화된 게시글입니다.");
        }

        return mapToResponse(post);
    }

    // 엔티티 -> DTO 변환
    private PostResponse mapToResponse(Post post){
        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getConcertId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
