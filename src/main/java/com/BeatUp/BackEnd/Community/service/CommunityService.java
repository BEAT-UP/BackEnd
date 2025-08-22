package com.BeatUp.BackEnd.Community.service;


import com.BeatUp.BackEnd.Community.Post.dto.request.CreatePostRequest;
import com.BeatUp.BackEnd.Community.Post.dto.response.PostResponse;
import com.BeatUp.BackEnd.Community.Post.entity.Post;
import com.BeatUp.BackEnd.User.entity.UserProfile;
import com.BeatUp.BackEnd.Concert.repository.ConcertRepository;
import com.BeatUp.BackEnd.Community.Post.repository.PostRepository;
import com.BeatUp.BackEnd.User.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional
    public PostResponse createPost(UUID authorId, CreatePostRequest request){
        // 1. 프로필 완성 여부 확인
        UserProfile profile = userProfileRepository.findByUserId(authorId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다"));

        if(!profile.isProfileCompleted()){
            throw new IllegalArgumentException("프로필을 먼저 완성해주세요");
        }

        // 2. Concert 존재 여부 확인
        if(!concertRepository.existsById(request.getConcertId())){
            throw new IllegalArgumentException("존재하지 않는 공연입니다.");
        }

        // 3. 게시글 새성
        Post post = new Post(authorId, request.getConcertId(),
                request.getTitle(), request.getContent());
        Post saved = postRepository.save(post);

        return mapToResponse(saved);
    }

    // 게시글 목록 조회
    public List<PostResponse> getPosts(UUID concertId, String query){
        List<Post> posts = postRepository.findByConcertIdAndQueryAndStatus(
                concertId, query, "ACTIVE"
        );
        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    public Optional<PostResponse> getPostById(UUID id){
        return postRepository.findById(id)
                .filter(post -> "ACTIVE".equals(post.getStatus()))
                .map(this::mapToResponse);
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
