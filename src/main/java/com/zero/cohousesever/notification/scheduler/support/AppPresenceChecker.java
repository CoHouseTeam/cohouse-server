package com.zero.cohousesever.notification.scheduler.support;

/**
 * 앱 접속 여부(online/active) 확인용.
 * - 실제 구현을 세션/웹소켓/레디스 등으로 교체하세요.
 */
public interface AppPresenceChecker {
    boolean isActive(Long memberId);
}