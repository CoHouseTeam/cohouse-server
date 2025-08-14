package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.notification.dto.NotificationRequestDto;
import com.zero.cohousesever.notification.dto.NotificationResponseDto;
import com.zero.cohousesever.notification.dto.NotificationSettingDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    // 특정 사용자의 알림 목록 조회
    public List<NotificationResponseDto> getNotifications(Long memberId, String type, Boolean read) {
        return null;
    }

    // 알림 생성
    public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
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