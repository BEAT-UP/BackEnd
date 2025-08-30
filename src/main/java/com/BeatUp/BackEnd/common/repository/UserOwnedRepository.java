package com.BeatUp.BackEnd.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface UserOwnedRepository<T> extends BaseRepository<T> {

    // 사용자별 조회(최신순)
    List<T> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // 사용자별 페이징 조회
    Page<T> findByUserId(UUID userId, Pageable pageable);
}
