package com.zero.cohousesever.notification.push.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * FirebaseConfig
 * - 서버 부팅 시 FirebaseApp을 1회 초기화한다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    /**
     * - 스프링 컨텍스트가 준비되면 FirebaseApp 1회 초기화.
     * - 이미 초기화된 경우 재초기화하지 않음.
     */
    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return; // 이미 초기화됨
            }
//            InputStream credStream = resolveCredentialStream();
//            GoogleCredentials creds = GoogleCredentials.fromStream(credStream);
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(creds)
//                    .setProjectId(projectId)
//                    .build();

//            FirebaseApp.initializeApp(options);
            log.info("[FCM] FirebaseApp initialized for projectId={}", projectId);
        } catch (Exception e) {
            log.error("[FCM] init fail: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FCM_INIT_FAIL);
        }
    }

    private InputStream resolveCredentialStream() {
        String b64 = System.getenv("FIREBASE_CREDENTIALS_B64");
        System.out.println(b64);
        if (b64 != null && !b64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(b64);
            return new ByteArrayInputStream(decoded);
        }
        // 자격증명이 전혀 없으면 공통 예외
        throw new CustomException(ErrorCode.FCM_INIT_FAIL);
    }
}