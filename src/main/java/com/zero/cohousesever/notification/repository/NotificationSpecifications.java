package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.type.NotificationType;
import org.springframework.data.jpa.domain.Specification;

/**
 * 알림 검색용 Specification
 * - memberId, type, read 여부 필터
 */
public final class NotificationSpecifications {

    private NotificationSpecifications() {}

    public static Specification<Notification> memberIdEq(Long memberId) {
        return (root, q, cb) -> cb.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Notification> typeEq(NotificationType type) {
        if (type == null) return null;
        return (root, q, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Notification> readEq(Boolean read) {
        if (read == null) return null;
        return (root, q, cb) -> cb.equal(root.get("isRead"), read);
    }
}