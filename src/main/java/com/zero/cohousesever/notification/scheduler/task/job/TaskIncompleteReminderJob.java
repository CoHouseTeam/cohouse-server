package com.zero.cohousesever.notification.scheduler.task.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.scheduler.support.AppPresenceChecker;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * 매일 22:00 "당일 미완료" 리마인드
 * - TODO: 실제로는 할일 도메인에 질의해서 미완료가 있을 때만 발송,
 *         없으면 자기 자신 해제 가능합니다.
 */
@Component
@RequiredArgsConstructor
public class TaskIncompleteReminderJob implements Job {

    private final NotificationService notificationService;
    private final AppPresenceChecker presenceChecker;
    // private final TaskQueryService taskQueryService; // 후속 주입

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");

        // TODO: 실제 확인 로직으로 대체
        boolean hasIncompleteToday = true; // taskQueryService.hasIncompleteToday(memberId)

        if (hasIncompleteToday) {
            boolean active = presenceChecker.isActive(memberId);
            notificationService.create(
                    memberId,
                    new NotificationCreateRequest(
                            NotificationType.TASK,
                            "할일 미완료",
                            "오늘 맡은 할일 중 미완료 항목이 있습니다."
                    ),
                    active
            );
        } else {
            // 더 이상 필요 없으면 자기 자신 해제 가능
            // context.getScheduler().deleteJob(context.getJobDetail().getKey());
        }
    }
}