package com.zero.cohousesever.notification.scheduler.support;

import org.springframework.stereotype.Component;

/** 기본 구현: 항상 비접속(false) */
@Component
public class NoopAppPresenceChecker implements AppPresenceChecker {
    @Override
    public boolean isActive(Long memberId) {
        return false;
    }
}