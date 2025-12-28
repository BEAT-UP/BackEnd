package com.BeatUp.BackEnd.common.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * 테스트 환경에서 FirebaseMessaging을 Mock으로 제공
 */
@Configuration
@Profile("test")
public class TestFirebaseConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(FirebaseMessaging.class)
    public FirebaseMessaging firebaseMessaging() {
        return mock(FirebaseMessaging.class);
    }
}

