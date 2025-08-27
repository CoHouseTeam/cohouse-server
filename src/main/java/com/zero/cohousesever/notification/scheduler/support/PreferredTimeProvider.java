package com.zero.cohousesever.notification.scheduler.support;

import com.zero.cohousesever.notification.type.NotificationType;

import java.time.LocalTime;
import java.util.Optional;

/**
 * 사용자별 알림 선호 시각 제공자.
 * - 실제 구현은 멤버/설정 도메인에서 주입하세요.
 * - 없으면 Optional.empty()를 반환하면 기본 시각으로 전송됩니다.
 */
public interface PreferredTimeProvider {
    Optional<LocalTime> getPreferredTime(Long memberId, NotificationType type);
}