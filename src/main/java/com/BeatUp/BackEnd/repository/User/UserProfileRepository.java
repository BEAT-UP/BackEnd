package com.BeatUp.BackEnd.repository.User;

import com.BeatUp.BackEnd.entity.User.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    // userId로 profile 찾기
    Optional<UserProfile> findByUserId(UUID userId);
}
