package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.notification.push.port.PushCommand;
import com.zero.cohousesever.notification.push.port.PushSender;
import com.zero.cohousesever.notification.service.push.DeviceTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 서비스 계층이 간단히 호출할 수 있는 푸시 발송 브리지.
 * - 멤버의 활성 토큰을 조회해서 즉시 FCM 발송.
 * - 토큰이 없으면 CustomException 대신 조용히 스킵(정책에 따라 로깅만).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushBridge {

    private final PushSender pushSender;             // 실제 발송(FirebasePushSender)
    private final DeviceTokenProvider tokenProvider; // 토큰 조회(JpaDeviceTokenProvider)

    /**
     * 즉시 푸시 발송 헬퍼
     *
     * @param memberId 대상 회원 ID
     * @param title    푸시 제목
     * @param body     푸시 본문
     * @param data     추가 데이터(Map, nullable 가능)
     */
    public void sendNow(Long memberId, String title, String body, Map<String, String> data) {
        tokenProvider.findActiveTokenByMemberId(memberId).ifPresentOrElse(token -> {
            // 토큰이 있으면 푸시 발송
            pushSender.send(
                    PushCommand.builder()
                            .memberId(memberId)
                            .token(token)
                            .title(title)
                            .body(body)
                            .data(data)
                            .build()
            );
            // 성공 후 토큰 사용 흔적 업데이트
            tokenProvider.touchTokenUse(token);
        }, () -> {
            // 토큰이 없으면 로깅만
            log.warn("[FCM] no active token for memberId={}", memberId);
        });
    }
}