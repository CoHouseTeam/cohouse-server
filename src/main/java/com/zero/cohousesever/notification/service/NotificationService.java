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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationPolicy notificationPolicy;
    private final NotificationPushBridge pushBridge;

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
                .title(safeTitle(req))
                .content(req.getContent())
                .isRead(false)
                .readAt(null)
                .status(NotificationStatus.ACTIVE)
                .build();

        Notification saved = notificationRepository.save(entity);

        // 타입별 설정 1건 로드 (없으면 Optional.empty)
        Optional<NotificationSetting> setting =
                notificationSettingRepository.findByMemberId(memberId);
        // 정책 판단
        DeliveryDecision decision = notificationPolicy.decide(saved, setting, isAppActive);

        if (decision == DeliveryDecision.SEND_NOW) {
            pushBridge.sendNow(
                    memberId,
                    saved.getTitle(),
                    saved.getContent(),
                    buildPushData(saved)
            );
        }

        return NotificationResponse.from(saved);
    }

    /**
     * title이 비어있을 때 타입별 기본 제목 제공
     */
    private String safeTitle(NotificationCreateRequest req) {
        String t = req.getTitle();
        if (t != null && !t.isBlank()) return t;
        switch (req.getType()) {
            case TASK:          return "할일 알림";
            case SETTLEMENT:    return "정산 알림";
            case ANNOUNCEMENT:  return "공지 알림";
            case DELETE_REQUEST:return "탈퇴 요청 알림";
            default:            return "알림";
        }
    }

    /**
     * 푸시 데이터 빌드(FCM data payload)
     */
    private Map<String, String> buildPushData(Notification n) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(n.getId()));
        data.put("type", n.getType().name());
        if (n.getGroupId() != null)
            data.put("groupId", String.valueOf(n.getGroupId()));
        if (n.getTaskId() != null)
            data.put("taskId", String.valueOf(n.getTaskId()));
        if (n.getSettlementId() != null)
            data.put("settlementId", String.valueOf(n.getSettlementId()));
        return data;
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
    @Transactional
    public void softDeleteAll(Long memberId) {
        notificationRepository.softDeleteAllByMember(
                memberId, NotificationStatus.DELETED, LocalDateTime.now()
        );
    }

    /**
     * 보관기간이 지난 알림을 일괄 소프트 딜리트 (배치/스케줄러용)
     */
    @Transactional
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
    @Transactional
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