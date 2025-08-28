package com.BeatUp.BackEnd.Community.Post.repository;

import com.BeatUp.BackEnd.Community.Post.entity.Post;
import com.BeatUp.BackEnd.common.repository.StatusRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 검색 기능(공연 필터 + 키워드 검색)
    @Query("SELECT p FROM Post p WHERE p.status = :status AND " +
            "(:concertId IS NULL OR p.concertId = :concertId) AND " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByConcertIdAndQueryAndStatus(
            @Param("concertId") UUID concertId,
            @Param("query") String query,
            @Param("status") String status
    );

    // 새로운 메서드 추가(Status Repository 기본 기능 활용)
    List<Post> findByConcertIdAndStatus(UUID concertId, String status);

    @Override
    default String getEntityName(){
        return "Post";
    }
}
