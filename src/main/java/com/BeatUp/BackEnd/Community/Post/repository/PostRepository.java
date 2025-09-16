package com.BeatUp.BackEnd.Community.Post.repository;

import com.BeatUp.BackEnd.Community.Post.entity.Post;
import com.BeatUp.BackEnd.common.repository.StatusRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends StatusRepository<Post> {

    // 공연별 게시글 조회
    default List<Post> findByConcertIdAndStatusOrderByCreatedAtDesc(UUID concertId, String status) {
        // 이 부분은 커스텀 쿼리가 필요하므로 기존 방식 유지하거나 Specification 활용
        return findByConcertIdAndStatus(concertId, status);
    }

    // 새로운 메서드 추가(Status Repository 기본 기능 활용)
    List<Post> findByConcertIdAndStatus(UUID concertId, String status);

    @Override
    default String getEntityName(){
        return "Post";
    }
}
