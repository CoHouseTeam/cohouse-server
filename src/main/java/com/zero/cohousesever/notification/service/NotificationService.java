package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.policy.DeliveryDecision;
import com.zero.cohousesever.notification.policy.NotificationPolicy;
import com.zero.cohousesever.notification.repository.NotificationRepository;
import com.zero.cohousesever.notification.repository.NotificationSettingRepository;
import com.zero.cohousesever.notification.type.NotificationStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationPolicy notificationPolicy;

    @PersistenceContext
    private EntityManager em;

    /**
     * 알림 생성 + 정책 판단
     * - DB 저장 후, 사용자 설정/앱 접속 여부를 바탕으로 SEND_NOW/SCHEDULED/SKIP 결정
     */
    public NotificationResponse create(Long memberId,
                                       NotificationCreateRequest req,
                                       boolean isAppActive
    ) {
        Notification entity = Notification.builder()
                .member(em.getReference(Member.class, memberId))
                .type(req.getType())
                .title(req.getTitle())
                .content(req.getContent())
                .isRead(false)
                .readAt(null)
                .status(NotificationStatus.ACTIVE)
                .build();

        Notification saved = notificationRepository.save(entity);

        // 타입별 설정 1건 로드 (없으면 Optional.empty)
        Optional<NotificationSetting> setting =
                notificationSettingRepository.findByMember_Id(memberId);
        // 정책 판단
        DeliveryDecision decision = notificationPolicy.decide(saved, setting, isAppActive);

        // 이 단계에서는 "결정만" 수행합니다.
        // - SEND_NOW  : 웹소켓 즉시 푸시로 보낼 파이프라인에 전달
        // - SCHEDULED : 사용자 지정 시각에 맞춰 스케줄러 큐에 적재
        // - SKIP      : 아무것도 안 함
        //
        // 실제 송신/스케줄링 연결은 후속 PR(#111 Scheduler)에서 붙입니다.

        return NotificationResponse.from(saved);
    }

    /**
     * 로그인 사용자의 알림 목록 조회
     */
    public List<NotificationResponse> list(Long memberId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Notification> list = notificationRepository.findByMember(
                memberId, cutoff, NotificationStatus.ACTIVE
        );
        return list.stream().map(NotificationResponse::from).toList();
    }

    /**
     * 사용자 알림 전체 소프트 딜리트
     */
    public void softDeleteAll(Long memberId) {
        notificationRepository.softDeleteAllByMember(
                memberId, NotificationStatus.DELETED, LocalDateTime.now()
        );
    }

    /**
     * 보관기간이 지난 알림을 일괄 소프트 딜리트 (배치/스케줄러용)
     */
    public int softDeleteOutdated() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return notificationRepository.softDeleteOlderThan(
                cutoff, NotificationStatus.DELETED, LocalDateTime.now()
        );
    }

    /**
     * 알림 읽음 처리
     * - 미읽음이면 읽음 처리, 이미 읽음이어도 성공(멱등)
     * - 존재하지 않거나 권한이 없으면 예외 발생
     */
    public void markAsRead(Long memberId, Long notificationId) {
        int updated = notificationRepository.markRead(
                notificationId, memberId, NotificationStatus.ACTIVE, LocalDateTime.now());

        if (updated > 0) return;

        Boolean readFlag = notificationRepository.findReadFlagForActiveMember(
                notificationId, memberId, NotificationStatus.ACTIVE);

        if (readFlag == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        // readFlag == true 이면 이미 읽음 → 멱등 성공 처리(예외 없음)
    }

    /**
     * 미읽음(미확인) 알림 개수: 30일 컷 + ACTIVE + isRead=false
     */
    public long getUnreadCount(Long memberId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return notificationRepository.countUnread(
                memberId, NotificationStatus.ACTIVE, cutoff);
    }
}