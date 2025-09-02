package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.notification.dto.NotificationSettingResponse;
import com.zero.cohousesever.notification.dto.NotificationSettingUpdateRequest;
import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.repository.NotificationSettingRepository;
import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * 알림 설정 서비스
 * - 조회 시 누락 타입은 기본값(true)로 채워 반환합니다.
 * - 수정은 upsert: 없으면 생성, 있으면 enabled만 갱신합니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * 조회(없으면 기본값 생성 후 반환)
     */
    public NotificationSettingResponse getOrCreate(Long memberId) {
        Optional<NotificationSetting> found = notificationSettingRepository.findByMemberId(memberId);
        if (found.isPresent()) {
            return NotificationSettingResponse.from(found.get());
        }

        // 기본값(모든 타입 ON) 생성
        Member ref = em.getReference(Member.class, memberId);
        NotificationSetting created = NotificationSetting.createDefault(ref);
        NotificationSetting saved = notificationSettingRepository.save(created);
        return NotificationSettingResponse.from(saved);
    }

    /**
     * 단일 타입 설정 변경(upsert)
     */
    public void update(Long memberId, NotificationSettingUpdateRequest request) {
        if (allNull(request)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST); // 최소 하나는 있어야 함
        }

        // 없으면 기본값 생성 후 갱신
        NotificationSetting setting = notificationSettingRepository
                .findByMemberId(memberId)
                .orElseGet(() -> NotificationSetting.createDefault(
                        em.getReference(Member.class, memberId)
                ));

        if (request.getTaskEnabled() != null) {
            setting.setTaskEnabled(request.getTaskEnabled());
        }
        if (request.getAnnouncementEnabled() != null) {
            setting.setAnnouncementEnabled(request.getAnnouncementEnabled());
        }
        if (request.getSettlementEnabled() != null) {
            setting.setSettlementEnabled(request.getSettlementEnabled());
        }

        notificationSettingRepository.save(setting);
    }

    /**
     * 모두 null인지 검사(부분 갱신 유효성)
     */
    private boolean allNull(NotificationSettingUpdateRequest req) {
        return Objects.isNull(req.getTaskEnabled())
                && Objects.isNull(req.getAnnouncementEnabled())
                && Objects.isNull(req.getSettlementEnabled());
    }

    /**
     * 멤버의 알림 설정을 조회.
     * 없으면 "기본값(전부 ON)"으로 생성 후 반환.
     */
    public NotificationSetting get(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    // 영속 프록시(쿼리 X)로 Member 참조
                    Member ref = memberRepository.getReferenceById(memberId);
                    NotificationSetting created = NotificationSetting.createDefault(ref);
                    return notificationSettingRepository.save(created);
                });
    }

    /**
     * 특정 타입의 알림이 켜져있는지 여부 (편의 메서드)
     */
    public boolean isEnabled(Long memberId, NotificationType type) {
        return notificationSettingRepository.findByMemberId(memberId)
                .map(s -> s.isEnabled(type))
                .orElse(true); // 설정이 아직 없으면 기본 ON 취급
    }
}