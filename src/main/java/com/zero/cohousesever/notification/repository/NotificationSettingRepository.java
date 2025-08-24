package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.type.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 설정 리포지토리
 * - 회원 전체 설정과 특정 타입의 설정을 조회합니다.
 */
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findByMember_Id(Long memberId);

    Optional<NotificationSetting> findByMember_IdAndType(Long memberId, NotificationType type);
}