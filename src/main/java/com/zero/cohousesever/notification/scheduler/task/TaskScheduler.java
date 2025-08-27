package com.zero.cohousesever.notification.scheduler.task;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.scheduler.support.AppPresenceChecker;
import com.zero.cohousesever.notification.scheduler.support.PreferredTimeProvider;
import com.zero.cohousesever.notification.scheduler.task.job.TaskDailySummaryJob;
import com.zero.cohousesever.notification.scheduler.task.job.TaskIncompleteReminderJob;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;
import java.util.TimeZone;

/**
 * 할일 스케줄러:
 * - 사용자 지정시각(없으면 08:00) "오늘의 할일" 요약
 * - 22:00 "미완료" 리마인드
 * - 앱 접속 중이면 즉시 알림
 */
@Service
@RequiredArgsConstructor
public class TaskScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime DEFAULT_SUMMARY_TIME = LocalTime.of(8, 0);
    private static final int INCOMPLETE_REMINDER_HOUR = 22;

    private final Scheduler scheduler;
    private final PreferredTimeProvider timeProvider;
    private final AppPresenceChecker presenceChecker;
    private final NotificationService notificationService;

    /** 매일 사용자 지정시각(없으면 08:00) 요약 등록 + 접속중이면 즉시 1회 발송 */
    public void registerDailySummary(Long memberId) throws SchedulerException {
        LocalTime time = timeProvider
                .getPreferredTime(memberId, NotificationType.TASK)
                .orElse(DEFAULT_SUMMARY_TIME);

        // 접속 중이면 즉시 1회 발송
        if (presenceChecker.isActive(memberId)) {
            notificationService.create(
                    memberId,
                    new NotificationCreateRequest(
                            NotificationType.TASK,
                            "오늘의 할일",
                            "지정 시각(" + time + ")에 오늘 할일 요약을 전송합니다."
                    ),
                    true
            );
        }

        // 그리고 매일 스케줄 등록
        JobDetail job = JobBuilder.newJob(TaskDailySummaryJob.class)
                .withIdentity("task:sum:" + memberId)
                .usingJobData("memberId", memberId)
                .usingJobData("hhmm", time.toString())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("task:sum:trg:" + memberId)
                .withSchedule(CronScheduleBuilder
                        .dailyAtHourAndMinute(time.getHour(), time.getMinute())
                        .inTimeZone(TimeZone.getTimeZone(KST)))
                .startNow()
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    /** 매일 22:00 미완료 리마인드 등록 (실제 발송 시 presence 반영) */
    public void registerDailyIncompleteReminder(Long memberId) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(TaskIncompleteReminderJob.class)
                .withIdentity("task:inc:" + memberId)
                .usingJobData("memberId", memberId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("task:inc:trg:" + memberId)
                .withSchedule(CronScheduleBuilder
                        .dailyAtHourAndMinute(INCOMPLETE_REMINDER_HOUR, 0)
                        .inTimeZone(TimeZone.getTimeZone(KST)))
                .startNow()
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}