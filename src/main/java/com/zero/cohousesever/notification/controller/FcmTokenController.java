package com.zero.cohousesever.notification.controller;

import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.notification.dto.FcmTokenRegisterRequest;
import com.zero.cohousesever.notification.dto.FcmTokenResponse;
import com.zero.cohousesever.notification.service.push.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    /**
     * POST /api/fcm-tokens : FCM 토큰 등록(업서트)
     * - 동일 토큰 존재 시 활성화 + 소유자 재지정(다른 계정에서 로그인한 경우)
     * - 없으면 신규 생성
     * - 201 Created + Location: /api/fcm-tokens/{id}
     */
    @PostMapping
    public ResponseEntity<FcmTokenResponse> register(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody FcmTokenRegisterRequest request
    ) {
        FcmTokenResponse saved = fcmTokenService.register(principal.getId(), request.getToken());
        return ResponseEntity.created(URI.create("/api/fcm-tokens/" + saved.getId()))
                .body(saved);
    }

    /**
     * DELETE /api/fcm-tokens/{tokenId} : 토큰 비활성화(로그아웃 등)
     * - 소유자 본인 토큰만 대상
     * - 대상이 없거나 이미 비활성이어도 멱등(204)
     */
    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long tokenId
    ) {
        fcmTokenService.deactivate(principal.getId(), tokenId);
        return ResponseEntity.noContent().build();
    }
}