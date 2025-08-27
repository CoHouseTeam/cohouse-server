package com.zero.cohousesever.notification.scheduler.settlement.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * 매일 22:00 실행: “여전히 미송금”이면 정산 리마인드 알림 전송
 * - 도메인 연결 전까지 unpaid=true 임시값. 실제 연결 시 질의로 대체
 */
@Component
@RequiredArgsConstructor
public class SettlementReminderJob implements Job {

    private final NotificationService notificationService;
    // private final SettlementQueryPort settlementQueryPort; // 실제 연결 시 주입해서 사용

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId     = m.getLong("memberId");
        Long settlementId = m.getLong("settlementId");

        // TODO: 실제 구현으로 대체: boolean unpaid = settlementQueryPort.isUnpaid(settlementId, memberId);
        boolean unpaid = true;

        if (unpaid) {
            notificationService.create(
                    memberId,
                    new NotificationCreateRequest(
                            NotificationType.SETTLEMENT,
                            "정산 리마인드",
                            "아직 송금이 완료되지 않았습니다. (settlementId=" + settlementId + ")"
                    ),
                    false // 스케줄 실행 시 보통 미접속 가정. 정책상 SETTLEMENT는 즉시 발송 결정됨
            );
        } else {
            // 필요 시 자기 자신 예약 해제:
            // try { context.getScheduler().deleteJob(context.getJobDetail().getKey()); } catch (Exception ignore) {}
        }
    }
}