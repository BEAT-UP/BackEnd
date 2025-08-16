package com.BeatUp.BackEnd.repository.User;

import com.BeatUp.BackEnd.entity.User.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    // 이메일로 사용자 찾기 - Spring Data JPA가 메서드 이름을 보고 자동으로 쿼리 생성
    Optional<UserAccount> findByEmail(String email);

    // 이메일이 존재하는지 확인
    boolean existsByEmail(String email);
}
