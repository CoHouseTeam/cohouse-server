package com.zero.cohousesever.notification.scheduler.support;

import com.zero.cohousesever.notification.type.NotificationType;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Optional;

/** 임시 구현: 항상 비어있음(기본 시각 사용) */
@Component
public class NoopPreferredTimeProvider implements PreferredTimeProvider {
    @Override
    public Optional<LocalTime> getPreferredTime(Long memberId, NotificationType type) {
        return Optional.empty();
    }
}