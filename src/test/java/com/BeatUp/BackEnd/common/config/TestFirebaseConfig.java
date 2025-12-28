package com.BeatUp.BackEnd.common.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * 테스트 환경에서 FirebaseMessaging을 Mock으로 제공
 */
@TestConfiguration
@Profile("test")
public class TestFirebaseConfig {

    @Bean
    @Primary
    public FirebaseMessaging firebaseMessaging() {
        return mock(FirebaseMessaging.class);
    }
}

