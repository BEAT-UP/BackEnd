package com.BeatUp.BackEnd.controller;

import com.BeatUp.BackEnd.entity.User;
import com.BeatUp.BackEnd.entity.UserStatus;
import com.BeatUp.BackEnd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // 모든 사용자 조회
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // 사용자 ID로 조회
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // 테스트용 사용자 생성
    @PostMapping("/test")
    public ResponseEntity<User> createTestUser() {
        User testUser = User.builder()
                .email("test@beatup.com")
                .passwordHash("hashedpassword123")
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(testUser);
        return ResponseEntity.ok(savedUser);
    }
}
