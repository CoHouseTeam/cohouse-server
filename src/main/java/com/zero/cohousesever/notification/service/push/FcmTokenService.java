package com.zero.cohousesever.notification.service.push;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.dto.FcmTokenResponse;
import com.zero.cohousesever.notification.entity.DeviceToken;
import com.zero.cohousesever.notification.repository.DeviceTokenRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * FCM 토큰 업서트:
     * - 존재하면 활성화 + 최근 사용시각 갱신
     * - 다른 멤버 소유였으면 소유자 재지정
     */
    @Transactional
    public FcmTokenResponse register(Long memberId, String token) {
        if (token == null || token.isBlank()) {
            throw new CustomException(ErrorCode.FCM_TOKEN_MISSING);
        }

        DeviceToken saved = deviceTokenRepository.findByToken(token)
                .map(existing -> {
                    if (!existing.getMember().getId().equals(memberId)) {
                        existing.reassignOwner(em.getReference(Member.class, memberId));
                    }
                    existing.activate();
                    existing.markUsedNow();
                    return existing;
                })
                .orElseGet(() -> {
                    DeviceToken t = DeviceToken.builder()
                            .member(em.getReference(Member.class, memberId))
                            .token(token)
                            .active(true)
                            .build();
                    t.markUsedNow();
                    return t;
                });

        DeviceToken persisted = deviceTokenRepository.save(saved);
        return FcmTokenResponse.from(persisted);
    }

    /**
     * 토큰 비활성화(멱등): 소유자 확인 후 deactivate
     * - 대상이 없거나 소유자가 아니면 아무 동작 없이 성공(204)
     */
    @Transactional
    public void deactivate(Long memberId, Long tokenId) {
        deviceTokenRepository.findByIdAndMember_Id(tokenId, memberId)
                .ifPresent(DeviceToken::deactivate);
    }
}