package com.zero.cohousesever.notification.scheduler.settlement.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 정산 미송금 리마인드 잡(매일 22:00)
 * - TODO: 실제로는 정산 서비스에 질의해 "여전히 미송금"이면 발송, 아니면 자기 자신 해제 가능
 */
@Component
@RequiredArgsConstructor
public class SettlementReminderJob implements Job {

    private final NotificationService notificationService;
    // private final SettlementQueryService settlementQueryService; // 후속 주입

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");
        Long settlementId = m.getLong("settlementId");

        // TODO: 실제 체크로 대체
        boolean unpaid = true; // settlementQueryService.isUnpaid(settlementId, memberId)

        if (unpaid) {
            NotificationCreateRequest req = NotificationCreateRequest.builder()
                    .type(NotificationType.SETTLEMENT)
                    .title("정산 리마인드")
                    .content("아직 송금이 완료되지 않았습니다. (settlementId=" + settlementId + ")")
                    .build();

            notificationService.create(memberId, req, false);
        } else {
            // 더 이상 필요 없으면 자기 자신 해제 가능
            // context.getScheduler().deleteJob(context.getJobDetail().getKey());
        }
    }
}