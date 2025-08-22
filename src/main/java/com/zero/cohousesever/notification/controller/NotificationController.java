package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.notification.dto.NotificationRequestDto;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.dto.NotificationSettingDto;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 로그인 사용자의 알림 목록을 조회
     * 요청 파라미터:
     * - type: 알림 타입 필터 (예: TASK, SETTLEMENT, ANNOUNCEMENT). 없으면 전체 조회
     * - read: 읽음 여부 필터. 없으면 전체 조회
     * 반환:
     * - 30일 이내 ACTIVE 상태의 알림 리스트 (최신순)
     * 인증:
     * - 우선 @AuthenticationPrincipal 의 id 사용
     * - 없을 경우 X-Member-Id 헤더 값 사용 (테스트 목적)
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Boolean read
    ) {

        return ResponseEntity.ok(notificationService.getNotifications(principal.getId(), type, read));
    }

    /**
     * 로그인 사용자의 모든 알림을 소프트 딜리트
     * 동작:
     * - ACTIVE 상태의 알림을 DELETED로 전환
     * - deletedAt 시각 기록
     * 인증:
     * - 우선 @AuthenticationPrincipal 의 id 사용
     * - 없을 경우 X-Member-Id 헤더 값 사용 (테스트 목적)
     * 반환:
     * - 응답 본문 없음 (204 No Content 성격)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        notificationService.softDeleteAll(principal.getId());
       return ResponseEntity.noContent().build();
    }

    /**
     * 알림 생성
     * - 할일 등록, 공지 작성에서 내부적으로 호출
     */
    @PostMapping
    public NotificationResponse createNotification
    (@RequestBody NotificationRequestDto requestDto) {
        return notificationService.createNotification(requestDto);
    }

    /**
     * 알림 읽음 처리
     * - 대상 알림이 ACTIVE이고 미읽음이면 읽음 처리합니다.
     * - 이미 읽은 알림인 경우에도 성공으로 응답합니다.
     * - 대상이 없거나 권한이 없으면 예외를 반환합니다.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> read(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(principal.getId(), notificationId);
        return ResponseEntity.ok(Map.of("message", "알림 확인!"));
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
    public void updateSetting(@RequestBody NotificationSettingDto
                                      settingDto) {
        Long memberId = 1L;
        notificationService.updateSetting(memberId, settingDto);
    }
}