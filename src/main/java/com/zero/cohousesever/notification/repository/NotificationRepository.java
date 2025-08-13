package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberId(Long memberId);
    List<Notification> findByMemberIdAndRead(Long memberId, boolean read);
}