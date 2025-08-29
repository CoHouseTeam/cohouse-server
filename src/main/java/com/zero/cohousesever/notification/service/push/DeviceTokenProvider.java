package com.zero.cohousesever.notification.service.push;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.entity.DeviceToken;
import com.zero.cohousesever.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * DeviceToken 조회/갱신 전용 서비스
 * - 멤버의 최신 '활성' FCM 토큰을 조회
 * - 발송 성공 시 lastUsedAt 갱신
 */
@Service
@RequiredArgsConstructor
public class DeviceTokenProvider {

    private final DeviceTokenRepository deviceTokenRepository;

    /** 멤버의 최신 활성 토큰 문자열(Optional) */
    public Optional<String> findActiveTokenByMemberId(Long memberId) {
        // 1순위: lastUsedAt 기준 최신
        Optional<DeviceToken> v1 = deviceTokenRepository
                .findTopByMember_IdAndActiveTrueOrderByLastUsedAtDesc(memberId);
        if (v1.isPresent()) return v1.map(DeviceToken::getToken);

        // 2순위: updatedAt 기준(초기 데이터/마이그레이션 대비)
        return deviceTokenRepository
                .findTopByMember_IdAndActiveTrueOrderByUpdatedAtDesc(memberId)
                .map(DeviceToken::getToken);
    }

    /** 없으면 예외 */
    public String getActiveTokenOrThrow(Long memberId) {
        return findActiveTokenByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_TOKEN_MISSING));
    }

    /** 발송 성공 등 사용 흔적 갱신 */
    public void touchTokenUse(String token) {
        // 토큰을 찾아 최근 사용 시각을 now로 갱신
        deviceTokenRepository.findByToken(token).ifPresent(DeviceToken::markUsedNow);
    }
}