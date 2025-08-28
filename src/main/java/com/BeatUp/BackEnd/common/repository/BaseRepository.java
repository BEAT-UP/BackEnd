package com.BeatUp.BackEnd.common.repository;

import com.BeatUp.BackEnd.common.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, UUID> {

    /**
     * ID로 엔티티를 찾아 반환하며, 없으면 예외를 던진다
     */
    default T findOrThrow(UUID id){
        return findById(id).orElseThrow(() ->
        new ResourceNotFoundException(getEntityName() + "not found with id: " + id));
    }

    /**
     * 엔티티명을 반환. 각 Repository에서 구현
     */
    default String getEntityName(){
        return "Entity"; // 기본값, 각 Repository에서 오버라이드
    }
}
