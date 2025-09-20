package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 알림 설정 리포지토리
 * - 회원 전체 설정과 특정 타입의 설정을 조회합니다.
 */
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    /**
     * 회원 ID로 알림 설정 조회
     */
    Optional<NotificationSetting> findByMemberId(Long memberId);

//    /**
//     * 회원 알림 설정 존재 여부 확인
//     */
//    boolean existsByMemberId(Long memberId);
}