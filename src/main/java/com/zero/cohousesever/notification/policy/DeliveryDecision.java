package com.zero.cohousesever.notification.policy;

/**
 * 알림 발송 정책 결과
 * - SEND_NOW: 즉시 발송 (앱 접속중 / 긴급 알림)
 * - SCHEDULED: 예약 발송 (사용자/시스템 설정 시간대)
 * - SKIP: 발송 안 함 (사용자가 끔)
 */
public enum DeliveryDecision {
    SEND_NOW,
    SCHEDULED,
    SKIP
}