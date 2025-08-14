package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMember(Member member);

    // 읽은 알림/읽지 않은 알림 조회 시 사용
    List<Notification> findByMemberIdAndIsRead(Long memberId, boolean isRead);
}