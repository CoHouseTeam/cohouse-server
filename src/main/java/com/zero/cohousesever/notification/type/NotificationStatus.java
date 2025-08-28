package com.zero.cohousesever.notification.type;

/**
 * 알림 상태
 * - ACTIVE: 유효(조회 대상)
 * - DELETED: 사용자가 전체삭제/배치정리로 삭제된 상태(소프트 딜리트)
 */
public enum NotificationStatus {
    ACTIVE,
    DELETED
}