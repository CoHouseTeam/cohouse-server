package com.zero.cohousesever.notification.scheduler.task.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
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

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");
        String hhmm = m.getString("hhmm");

        // TODO: 실제 오늘자 할일 목록을 조회해 본문 구성 가능
        NotificationCreateRequest req = NotificationCreateRequest.builder()
                .type(NotificationType.TASK)
                .title("오늘의 할일")
                .content("지정 시각(" + hhmm + ")에 오늘 할일 요약을 전송합니다.")
                .build();

        notificationService.create(memberId, req, false);
    }
}