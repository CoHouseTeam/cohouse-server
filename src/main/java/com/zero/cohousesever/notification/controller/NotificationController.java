package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.notification.dto.NotificationRequestDto;
import com.zero.cohousesever.notification.dto.NotificationResponseDto;
import com.zero.cohousesever.notification.dto.NotificationSettingDto;
import com.zero.cohousesever.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public List<NotificationResponseDto> getNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read
    ) {
        Long memberId = 1L; // TODO: 인증 정보에서 가져오기
        return notificationService.getNotifications(memberId, type, read);
    }

    /**
     * 알림 생성
     * - 할일 등록, 공지 작성에서 내부적으로 호출
     */
    @PostMapping
    public NotificationResponseDto createNotification(@RequestBody NotificationRequestDto requestDto) {
        return notificationService.createNotification(requestDto);
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    /**
     * 알림 설정 조회 - 로그인한 사용자 본인 것만
     */
    @GetMapping("/settings")
    public List<NotificationSettingDto> getSettings() {
        Long memberId = 1L;
        return notificationService.getNotificationSettings(memberId);
    }

    /**
     * 알림 설정 변경
     */
    @PutMapping("/settings")
    public void updateSetting(@RequestBody NotificationSettingDto settingDto) {
        Long memberId = 1L;
        notificationService.updateSetting(memberId, settingDto);
    }
}