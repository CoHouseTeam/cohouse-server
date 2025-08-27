package com.zero.cohousesever.notification.scheduler.task.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.scheduler.support.AppPresenceChecker;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Component;

/** 매일 사용자 지정시각(없으면 08:00) "오늘의 할일" 요약 전송 */
@Component
@RequiredArgsConstructor
public class TaskDailySummaryJob implements Job {

    private final NotificationService notificationService;
    private final AppPresenceChecker presenceChecker;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");
        String hhmm   = m.getString("hhmm");

        boolean active = presenceChecker.isActive(memberId);

        // TODO: 실제 오늘자 할일 목록을 조회해 본문 구성 가능
        notificationService.create(
                memberId,
                new NotificationCreateRequest(
                        NotificationType.TASK,
                        "오늘의 할일",
                        "지정 시각(" + hhmm + ")에 오늘 할일 요약을 전송합니다."
                ),
                active
        );
    }
}