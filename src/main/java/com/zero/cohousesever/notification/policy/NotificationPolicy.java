package com.zero.cohousesever.notification.policy;

import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.type.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 알림 발송 정책 결정 레이어
 * - 앱 접속 상태, 알림 타입, 사용자 설정값을 기반으로 발송 여부/시점을 결정
 */
@Component
public class NotificationPolicy {

    /**
     * 알림 발송 정책
     * - 입력: 생성된 알림, 타입별 사용자 설정, 앱 접속 여부
     * - 출력: SEND_NOW / SCHEDULED / SKIP
     */
    public DeliveryDecision decide(Notification notification,
                                   Optional<NotificationSetting> typeSettingOpt,
                                   boolean isAppActive) {

        NotificationType type = notification.getType();

        // 1) 타입별 설정 OFF ⇒ SKIP
        //    기본값: 설정 레코드가 없으면 ON 으로 간주.
        boolean typeEnabled = typeSettingOpt.map(s -> s.isEnabled(type)).orElse(true);
        if (!typeEnabled) {
            return DeliveryDecision.SKIP;
        }

        // 2. 긴급 알림 (예: 탈퇴 요청)은 즉시 발송
        if (NotificationType.SETTLEMENT.equals(type) || NotificationType.DELETE_REQUEST.equals(type)) {
            return DeliveryDecision.SEND_NOW;
        }

        // 3. 앱 접속 중이면 즉시 발송
        if (isAppActive) {
            return DeliveryDecision.SEND_NOW;
        }
        // 4. 앱 접속 X → 예약 발송
        return DeliveryDecision.SCHEDULED;
    }
}