package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.dto.UnreadCountResponse;
import com.zero.cohousesever.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 생성
     * - @Valid 로 요청값 검증
     * - 201 Created + Location 헤더(/api/notifications/{id})
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestHeader(defaultValue = "false") boolean isAppActive,
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        NotificationResponse created = notificationService.create(
                principal.getId(), request, isAppActive);

        return ResponseEntity.created(URI.create("/api/notifications/" + created.getId()))
                .body(created);
    }

    /**
     * 알림 목록 조회
     * - 최근 30일, ACTIVE 상태, 최신순
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(notificationService.list(principal.getId()));
    }

    /**
     * 사용자 알림 전체 삭제 (소프트 딜리트)
     * - ACTIVE 상태의 알림을 DELETED로 전환
     * - 204 No Content
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
     * - 성공/이미 읽음: 204 No Content
     * - 대상 없음/권한 없음: 예외를 반환
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