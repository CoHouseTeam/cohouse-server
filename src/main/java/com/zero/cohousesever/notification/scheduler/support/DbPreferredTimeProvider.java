package com.zero.cohousesever.notification.scheduler.support;

import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.repository.NotificationSettingRepository;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Optional;

/**
 * 실제 구현: 알림 설정(NotificationSetting)에서 선호 시각을 조회
 * - 설정이 없으면 기획 기본값을 제공
 *   ANNOUNCEMENT: 22:00
 *   TASK        : 08:00
 */
@Component
@RequiredArgsConstructor
public class DbPreferredTimeProvider implements PreferredTimeProvider {

    private final NotificationSettingRepository settingRepository;

    @Override
    public Optional<LocalTime> getPreferredTime(Long memberId, NotificationType type) {
        // 설정 한 번 조회 (없으면 빈 Optional)
        Optional<NotificationSetting> opt = settingRepository.findByMemberId(memberId);

        // 타입별로 선호 시각(or 기본값) 결정
        switch (type) {
            case ANNOUNCEMENT: {
                LocalTime t = opt.map(NotificationSetting::getAnnouncementTime).orElse(LocalTime.of(22, 0));
                return Optional.of(t);
            }
            case TASK: {
                LocalTime t = opt.map(NotificationSetting::getTaskTime).orElse(LocalTime.of(8, 0));
                return Optional.of(t);
            }
            default:
                // 다른 타입은 정책상 별도 기본시각 없음 → 빈값
                return Optional.empty();
        }
    }
}