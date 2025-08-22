package com.BeatUp.BackEnd.Community.Post.repository;

import com.BeatUp.BackEnd.Community.Post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    // 공연별 게시글 조회(최신순)
    List<Post> findByConcertIdAndStatusOrderByCreatedAtDesc(UUID concertId, String status);

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
}
