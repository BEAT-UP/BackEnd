package com.BeatUp.BackEnd.repository;

import com.BeatUp.BackEnd.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 전화번호로 사용자 찾기
    Optional<User> findByPhoneNumber(String phoneNumber);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 전화번호 중복 체크
    boolean existsByPhoneNumber(String phoneNumber);

    // 활성 사용자만 조회
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    java.util.List<User> findActiveUsers();

    // 특정 상태의 사용자 조회
    java.util.List<User> findByStatus(String status);
}