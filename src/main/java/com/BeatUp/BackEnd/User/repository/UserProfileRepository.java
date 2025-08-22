package com.BeatUp.BackEnd.User.repository;

import com.BeatUp.BackEnd.User.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    // userId로 profile 찾기
    Optional<UserProfile> findByUserId(UUID userId);
}
