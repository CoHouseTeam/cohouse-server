package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.repository.NotificationRepository;
import com.zero.cohousesever.notification.type.NotificationStatus;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 로그인 사용자의 알림 목록 조회
     */
    public List<NotificationResponse> getNotifications(Long memberId, NotificationType type, Boolean read) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Notification> list = notificationRepository.findByMember(
                memberId, cutoff, NotificationStatus.ACTIVE, type, read
        );
        return list.stream().map(NotificationResponse::from).toList();
    }

    /**
     * 사용자의 모든 유효 알림을 소프트 딜리트
     */
    @Transactional
    public void softDeleteAll(Long memberId) {
        notificationRepository.softDeleteAllByMember(
                memberId, NotificationStatus.DELETED, LocalDateTime.now()
        );
    }

    /**
     * 보관기간이 지난 알림을 일괄 소프트 딜리트 (배치/스케줄러용)
     */
    @Transactional
    public int softDeleteOutdated() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return notificationRepository.softDeleteOlderThan(
                cutoff, NotificationStatus.DELETED, LocalDateTime.now()
        );
    }

    /**
     * 알림 읽음 처리
     * - 미읽음이면 읽음 처리, 이미 읽음이어도 성공(멱등)
     * - 존재하지 않거나 권한이 없으면 예외 발생
     */
    public void markAsRead(Long memberId, Long notificationId) {
        int updated = notificationRepository.markRead(
                notificationId, memberId, NotificationStatus.ACTIVE, LocalDateTime.now()
        );

        // 정상적으로 읽음 처리됨
        if (updated > 0) {
            return;
        }

        // 업데이트 0건: 존재/권한/상태를 확인하여 예외/멱등 처리 분기
        Boolean readFlag = notificationRepository.findReadFlagForActiveMember(
                notificationId, memberId, NotificationStatus.ACTIVE
        );

        if (readFlag == null) {
            throw new CustomException(ErrorCode.INVITE_CODE_INVALID);
        }
        // readFlag == true 인 경우: 이미 읽음 → 멱등 처리로 성공 간주
    }

    /**
     * 미읽음(미확인) 알림 개수: 30일 컷 + ACTIVE + isRead=false
     */
    public long getUnreadCount(Long memberId) {
        return notificationRepository.countUnread(
                memberId, NotificationStatus.ACTIVE, LocalDateTime.now().minusDays(30)
        );
    }

}