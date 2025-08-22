package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.dto.UnreadCountResponse;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * - ACTIVE 상태의 알림을 DELETED로 전환
     * - deletedAt 시각 기록
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        notificationService.softDeleteAll(principal.getId());
        return ResponseEntity.noContent().build();
    }


    /**
     * 알림 읽음 처리
     * - 대상 알림이 ACTIVE이고 미읽음이면 읽음 처리합
     * - 이미 읽은 알림인 경우에도 성공으로 응답
     * - 대상이 없거나 권한이 없으면 예외를 반환
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(principal.getId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 미읽음(미확인) 알림 개수 조회
     * - 배지(빨간 점/숫자) 표시에 사용
     * 응답 예: { "count": 3 }
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }
}