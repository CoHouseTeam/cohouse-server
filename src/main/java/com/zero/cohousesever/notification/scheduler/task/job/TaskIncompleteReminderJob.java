package com.zero.cohousesever.notification.scheduler.task.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.service.NotificationPushBridge;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import com.zero.cohousesever.task.service.TaskAssignmentService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 매일 22:00 "당일 미완료" 리마인드
 * - TODO: 실제로는 할일 도메인에 질의해서 미완료가 있을 때만 발송,
 * 없으면 자기 자신 해제 가능
 */
@Component
@RequiredArgsConstructor
public class TaskIncompleteReminderJob implements Job {

    private final NotificationService notificationService;
    private final NotificationPushBridge pushBridge;
    private final TaskAssignmentService taskAssignmentService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");

        boolean hasIncompleteToday = taskAssignmentService.hasIncompleteToday(memberId);

        if (!hasIncompleteToday) {
            // 필요시 스스로 해제: context.getScheduler().deleteJob(context.getJobDetail().getKey());
            return;
        }

        String title = "할일 미완료 알림";
        String content = "오늘 맡은 할일 중 완료되지 않은 항목이 있습니다. 지금 확인해 주세요.";

        NotificationCreateRequest req = NotificationCreateRequest.builder()
            .type(NotificationType.TASK)
            .title(title)
            .content(content)
            .build();

        NotificationResponse saved = notificationService.create(memberId, req, false);

        pushBridge.sendNow(
            memberId,
            title,
            content,
            Map.of(
                "type", "TASK_INCOMPLETE",
                "notificationId", String.valueOf(saved.getId())
            )
        );
    }
}