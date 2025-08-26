package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.notification.dto.NotificationSettingResponse;
import com.zero.cohousesever.notification.dto.NotificationSettingUpdateRequest;
import com.zero.cohousesever.notification.service.NotificationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 설정 API
 * - GET: 로그인 사용자 설정 목록 조회
 * - PUT: 특정 타입의 설정 on/off 변경
 */
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    /**
     * 설정 조회(없으면 기본 생성 후 반환)
     */
    @GetMapping
    public ResponseEntity<NotificationSettingResponse> getSettings(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        NotificationSettingResponse body = notificationSettingService.getOrCreate(principal.getId());
        return ResponseEntity.ok(body);
    }

    /**
     * 설정 변경(부분 갱신, 204)
     */
    @PutMapping
    public ResponseEntity<Void> updateSettings(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody NotificationSettingUpdateRequest request
    ) {
        notificationSettingService.update(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }
}