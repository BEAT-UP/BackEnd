package com.BeatUp.BackEnd.common.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StatusRepository <T> extends BaseRepository<T>{

    // 상태별 조회(기본 정렬)
    List<T> findByStatusOrderByCreatedAtDesc(String status);

    // 상태별 페이징 조회
    Page<T> findByStatus(String status, Pageable pageable);

    // 활성 상태 조회(기본값: ACTIVE)
    default List<T> findActive(){
        return findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    // 활성 상태 페이징 조회
    default Page<T> findActive(Pageable pageable){
        return findByStatus("ACTIVE", pageable);
    }
}
