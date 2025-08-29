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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * FirebaseConfig
 * - 서버 부팅 시 FirebaseApp을 1회 초기화한다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {
//
//    /**
//     * application.yml 에서 주입 (없어도 됨: 서비스 계정 JSON 안에 project_id가 들어있음)
//     */
//    @Value("${firebase.project-id:}")
//    private String projectId;
//
//    /**
//     * application.yml: 파일 경로 (환경변수 B64가 우선)
//     */
//    @Value("${firebase.credentials-path:}")
//    private String credentialsPath;
//
//    /**
//     * FirebaseApp 1회 초기화
//     * 우선순위:
//     * 1) env FIREBASE_CREDENTIALS_B64  (Base64 인코딩된 서비스계정 JSON)
//     * 2) env FIREBASE_CREDENTIALS_JSON (그냥 JSON 원문 통째로)
//     * 3) yml firebase.credentials-path  (파일 경로)
//     * 4) env GOOGLE_APPLICATION_CREDENTIALS (구글 표준 경로)
//     */
//    @PostConstruct
//    public void init() {
//        try {
//            if (!FirebaseApp.getApps().isEmpty()) {
//                log.info("[FCM] FirebaseApp already initialized. skip");
//                return;
//            }
//
//            try (InputStream credStream = resolveCredentialStream()) {
//                GoogleCredentials creds = GoogleCredentials.fromStream(credStream);
//
//                FirebaseOptions.Builder optBuilder = FirebaseOptions.builder().setCredentials(creds);
//                if (projectId != null && !projectId.isBlank()) {
//                    optBuilder.setProjectId(projectId);
//                }
//
//                FirebaseOptions options = optBuilder.build();
//                FirebaseApp.initializeApp(options);
//                log.info("[FCM] FirebaseApp initialized (projectId={})",
//                        (projectId == null || projectId.isBlank()) ? "from-credentials" : projectId);
//            }
//        } catch (CustomException e) {
//            // 이미 우리 에러코드면 그대로 전파
//            throw e;
//        } catch (Exception e) {
//            log.error("[FCM] init fail: {}", e.getMessage(), e);
//            throw new CustomException(ErrorCode.FCM_INIT_FAIL);
//        }
//    }
//
//    private InputStream resolveCredentialStream() {
//        // 1) Base64 환경변수
//        String b64 = System.getenv("FIREBASE_CREDENTIALS_B64");
//        if (b64 != null && !b64.isBlank()) {
//            try {
//                // 개행/공백 제거 후 디코딩
//                String cleaned = b64.replaceAll("\\s+", "");
//                byte[] decoded = Base64.getDecoder().decode(cleaned);
//                log.info("[FCM] credentials source = FIREBASE_CREDENTIALS_B64");
//                return new ByteArrayInputStream(decoded);
//            } catch (IllegalArgumentException badB64) {
//                log.error("[FCM] invalid FIREBASE_CREDENTIALS_B64: {}", badB64.getMessage());
//                throw new CustomException(ErrorCode.FCM_INIT_FAIL);
//            }
//        }
//
//        // 2) JSON 원문 환경변수
//        String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
//        if (json != null && !json.isBlank()) {
//            log.info("[FCM] credentials source = FIREBASE_CREDENTIALS_JSON");
//            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
//        }
//
//        // 3) yml 경로
//        if (credentialsPath != null && !credentialsPath.isBlank()) {
//            File f = new File(credentialsPath);
//            if (!f.exists() || !f.isFile()) {
//                log.error("[FCM] credentials-path not found: {}", credentialsPath);
//                throw new CustomException(ErrorCode.FCM_INIT_FAIL);
//            }
//            try {
//                log.info("[FCM] credentials source = credentials-path: {}", credentialsPath);
//                return new FileInputStream(f);
//            } catch (FileNotFoundException e) {
//                log.error("[FCM] credentials-path open fail: {}", e.getMessage());
//                throw new CustomException(ErrorCode.FCM_INIT_FAIL);
//            }
//        }
//
//        // 4) 구글 표준 env (개발자 PC/CI에서 종종 사용)
//        String gPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
//        if (gPath != null && !gPath.isBlank()) {
//            File f = new File(gPath);
//            if (f.exists() && f.isFile()) {
//                try {
//                    log.info("[FCM] credentials source = GOOGLE_APPLICATION_CREDENTIALS: {}", gPath);
//                    return new FileInputStream(f);
//                } catch (FileNotFoundException e) {
//                    log.error("[FCM] GOOGLE_APPLICATION_CREDENTIALS open fail: {}", e.getMessage());
//                    throw new CustomException(ErrorCode.FCM_INIT_FAIL);
//                }
//            }
//        }
//
//        // 아무 것도 못 찾음
//        log.error("[FCM] no credentials provided (FIREBASE_CREDENTIALS_B64 / FIREBASE_CREDENTIALS_JSON / credentials-path / GOOGLE_APPLICATION_CREDENTIALS)");
//        throw new CustomException(ErrorCode.FCM_INIT_FAIL);
//    }
}