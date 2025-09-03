package com.BeatUp.BackEnd.User.repository;

import com.BeatUp.BackEnd.User.entity.UserAccount;
import com.BeatUp.BackEnd.common.exception.ResourceNotFoundException;
import com.BeatUp.BackEnd.common.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends BaseRepository<UserAccount> {

    // 이메일로 사용자 찾기 - Spring Data JPA가 메서드 이름을 보고 자동으로 쿼리 생성
    Optional<UserAccount> findByEmail(String email);

    // 이메일이 존재하는지 확인
    boolean existsByEmail(String email);

    // Firebase UID로 사용자 찾기
    Optional<UserAccount> findByFirebaseUid(String firebaseUid);

    @Override
    default String getEntityName(){
        return "UserAccount";
    }

    // findOrThrow 활용한 편의 메서드
    default UserAccount findByEmailOrThrow(String email){
        return findByEmail(email).orElseThrow(() ->
        new ResourceNotFoundException("User not found with email: " + email));
    }
}
