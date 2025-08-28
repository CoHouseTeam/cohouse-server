package com.zero.cohousesever.notification.push.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    /**
     * FirebaseApp 1회 초기화.
     * - 1순위: env FIREBASE_CREDENTIALS_B64 (Base64-encoded JSON)
     * - 2순위: credentials-path (파일 경로)
     */
    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                // 이미 초기화됨
                return;
            }

            InputStream credStream = resolveCredentialStream();
            GoogleCredentials creds = GoogleCredentials.fromStream(credStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(creds)
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("[FCM] FirebaseApp initialized for projectId={}", projectId);
        } catch (Exception e) {
            log.error("[FCM] init fail: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FCM_INIT_FAIL);
        }
    }

    private InputStream resolveCredentialStream() throws Exception {
        String b64 = System.getenv("FIREBASE_CREDENTIALS_B64");
        if (b64 != null && !b64.isBlank()) {
            byte[] decoded = Base64.decodeBase64(b64);
            return new ByteArrayInputStream(decoded);
        }
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            return new FileInputStream(credentialsPath);
        }
        // 로컬 개발 편의를 위해: GOOGLE_APPLICATION_CREDENTIALS 표준 경로도 동작(환경에서 처리)
        // 아무 것도 없으면 실패
        throw new IllegalStateException("No Firebase credentials provided (env FIREBASE_CREDENTIALS_B64 or firebase.credentials-path)");
    }
}