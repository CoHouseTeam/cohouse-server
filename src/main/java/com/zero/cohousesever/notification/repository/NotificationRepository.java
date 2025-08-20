package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.type.NotificationStatus;
import com.zero.cohousesever.notification.type.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 리포지토리
 * - 최근 30일 + 소프트 딜리트 제외 + 단순 필터(type/read)
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 로그인 사용자의 유효 알림 목록 (정렬: createdAt DESC)
     * - :type, :read 가 null이면 해당 조건 생략
     */
    @Query("""
                SELECT n
                  FROM Notification n
                 WHERE n.member.id = :memberId
                   AND n.status = :active
                   AND n.createdAt >= :cutoff
                   AND (:type IS NULL OR n.type = :type)
                   AND (:read IS NULL OR n.isRead = :read)
                 ORDER BY n.createdAt DESC
            """)
    List<Notification> findByMember(Long memberId,
                                    LocalDateTime cutoff,
                                    NotificationStatus active,
                                    NotificationType type,
                                    Boolean read);

    /**
     * 사용자의 모든 유효 알림을 소프트 딜리트
     */
    @Modifying
    @Query("""
        UPDATE Notification n
           SET n.status = :deleted, n.deletedAt = :now
         WHERE n.member.id = :memberId AND n.status <> :deleted
    """)
    int softDeleteAllByMember(Long memberId, NotificationStatus deleted, LocalDateTime now);

    /**
     * 보관기간이 지난 알림을 일괄 소프트 딜리트(배치)
     */
    @Modifying
    @Query("""
        UPDATE Notification n
           SET n.status = :deleted, n.deletedAt = :now
         WHERE n.status <> :deleted AND n.createdAt < :cutoff
    """)
    int softDeleteOlderThan(LocalDateTime cutoff, NotificationStatus deleted, LocalDateTime now);
}