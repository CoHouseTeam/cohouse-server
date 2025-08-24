package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.notification.dto.NotificationSettingResponse;
import com.zero.cohousesever.notification.dto.NotificationSettingUpdateRequest;
import com.zero.cohousesever.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 설정 API
 * - GET: 로그인 사용자 설정 목록 조회
 * - PUT: 특정 타입의 설정 on/off 변경
 */
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService settingService;

    /**
     * 설정 목록 조회(누락 타입은 기본 true로 채워 반환)
     */
    @GetMapping
    public ResponseEntity<List<NotificationSettingResponse>> list(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(settingService.getSettings(principal.getId()));
    }

    /**
     * 설정 변경(upsert)
     */
    @PutMapping
    public ResponseEntity<Map<String, String>> update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody NotificationSettingUpdateRequest request
    ) {
        settingService.updateSetting(principal.getId(), request.getType(), request.isEnabled());
        return ResponseEntity.ok(Map.of("message", "알림 설정이 변경되었습니다."));
    }
}