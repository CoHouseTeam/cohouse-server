package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.notification.dto.NotificationRequestDto;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.dto.NotificationSettingDto;
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

    // 알림 생성
    public NotificationResponse createNotification(NotificationRequestDto requestDto) {
        return null;
    }

    // 알림 읽음 처리
    public void markAsRead(Long notificationId) {
    }

    // 특정 사용자의 알림 설정 조회
    public List<NotificationSettingDto> getNotificationSettings(Long memberId) {
        return null;
    }

    // 특정 사용자의 알림 설정 변경
    public void updateSetting(Long memberId, NotificationSettingDto settingDto) {
    }
}