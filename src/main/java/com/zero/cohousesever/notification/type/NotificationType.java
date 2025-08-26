package com.zero.cohousesever.notification.type;


/**
 * 알림 타입
 * TASK: 할일 알림
 * SETTLEMENT: 정산 알림
 * ANNOUNCEMENT: 공지사항 알림
 * DELETE_REQUEST: 탈퇴 요청 알림
 */
public enum NotificationType {
    TASK,           // 할일 알림
    SETTLEMENT,     // 정산 알림
    ANNOUNCEMENT,   // 공지사항 알림
    DELETE_REQUEST
}
