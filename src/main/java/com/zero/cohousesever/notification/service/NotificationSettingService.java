package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.dto.NotificationSettingResponse;
import com.zero.cohousesever.notification.dto.NotificationSettingUpdateRequest;
import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.repository.NotificationSettingRepository;
import com.zero.cohousesever.notification.type.NotificationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 알림 설정 서비스
 * - 조회 시 누락 타입은 기본값(true)로 채워 반환합니다.
 * - 수정은 upsert: 없으면 생성, 있으면 enabled만 갱신합니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository settingRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * 회원 전체 설정 조회(누락 타입은 기본 true로 보정)
     */
    public List<NotificationSettingResponse> getSettings(Long memberId) {
        // DB에 저장된 설정 로드
        List<NotificationSetting> stored = settingRepository.findByMember_Id(memberId);
        Map<NotificationType, Boolean> map = new EnumMap<>(NotificationType.class);
        for (NotificationSetting s : stored) {
            map.put(s.getType(), s.isEnabled());
        }

        // 모든 타입 기준으로 누락은 기본 true 채움
        List<NotificationSettingResponse> result = new ArrayList<>();
        for (NotificationType type : NotificationType.values()) {
            boolean enabled = map.getOrDefault(type, true);
            result.add(new NotificationSettingResponse(type, enabled));
        }
        return result;
    }

    /**
     * 단일 타입 설정 변경(upsert)
     * - @Transactional: 변경성 작업의 원자성/동시성 보장을 위해 필요합니다.
     */
    @Transactional
    public void updateSetting(Long memberId, NotificationType type, boolean isEnabled) {
        settingRepository.findByMember_IdAndType(memberId, type)
                .ifPresentOrElse(
                        // 존재하면 enabled만 갱신
                        s -> s.setEnabled(isEnabled),
                        // 없으면 생성(지연 로딩 프록시로 Member 참조)
                        () -> {
                            NotificationSetting created = NotificationSetting.builder()
                                    .member(em.getReference(Member.class, memberId))
                                    .type(type)
                                    .isEnabled(isEnabled)
                                    .build();
                            settingRepository.save(created);
                        }
                );
    }
}