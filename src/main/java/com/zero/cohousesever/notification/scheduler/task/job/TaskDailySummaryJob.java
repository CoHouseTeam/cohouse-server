package com.zero.cohousesever.notification.scheduler.task.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.service.NotificationPushBridge;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.service.TaskAssignmentService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 매일 사용자 지정시각(없으면 08:00) "오늘의 할일" 요약 전송
 */
@Component
@RequiredArgsConstructor
public class TaskDailySummaryJob implements Job {

    private final NotificationService notificationService;
    private final NotificationPushBridge pushBridge;
    private final TaskAssignmentService taskAssignmentService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");
        String hhmm = m.getString("hhmm");

        // 오늘자 할일 조회
        List<TaskAssignment> today = taskAssignmentService.getTodayAssignments(memberId);

        if (today == null || today.isEmpty()) {
            return;
        }

        String list = today.stream()
            .map(a -> "- " + a.getTemplate().getCategory())
            .collect(java.util.stream.Collectors.joining("\n"));

        String title = "오늘의 할일 (" + hhmm + ")";
        String content = list;

        // 알림 저장
        NotificationCreateRequest req = NotificationCreateRequest.builder()
            .type(NotificationType.TASK)
            .title(title)
            .content(content)
            .build();

        NotificationResponse saved = notificationService.create(memberId, req, false);

        // 4) 푸시 즉시 발송 (토큰 없으면 내부에서 스킵)
        pushBridge.sendNow(
            memberId,
            title,
            content,
            Map.of(
                "type", "TASK_SUMMARY",
                "notificationId", String.valueOf(saved.getId())
            )
        );
    }
}