package com.BeatUp.BackEnd.common.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.info("Firebase 비활성화됨");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) return;

        try {
            InputStream serviceAccount = null;
            String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

            if (envPath != null) {
                serviceAccount = new FileInputStream(envPath);
                log.info("Firebase 키 로드: 환경변수");
            } else {
                serviceAccount = getClass().getResourceAsStream("/firebase/beatup-7ca5d-firebase-adminsdk-fbsvc-c60582a84b.json");
                if (serviceAccount != null) {
                    log.info("Firebase 키 로드: classpath");
                }
            }

            if (serviceAccount == null) {
                log.warn("Firebase 키 없음 - 건너뜀");
                return;
            }

            try (InputStream stream = serviceAccount) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase 초기화 완료");
            }

        } catch (Exception e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage());
            // 애플리케이션은 계속 실행됨
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(){
        return FirebaseMessaging.getInstance();
    }
}
