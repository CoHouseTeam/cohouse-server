package com.zero.cohousesever.notification.scheduler.task;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.scheduler.support.PreferredTimeProvider;
import com.zero.cohousesever.notification.scheduler.task.job.TaskDailySummaryJob;
import com.zero.cohousesever.notification.scheduler.task.job.TaskIncompleteReminderJob;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * 할일 스케줄러:
 * - 사용자 지정시각(없으면 08:00) "오늘의 할일" 요약
 * - 22:00 "미완료" 리마인드
 */
@Service
@RequiredArgsConstructor
public class TaskScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime DEFAULT_SUMMARY_TIME = LocalTime.of(8, 0);
    private static final int INCOMPLETE_REMINDER_HOUR = 22;

    private final Scheduler scheduler;
    private final PreferredTimeProvider timeProvider;

    /**
     * 매일 사용자 지정시각(없으면 08:00) 요약 등록
     */
    public void registerDailySummary(Long memberId) {
        try {
            LocalTime time = timeProvider
                    .getPreferredTime(memberId, NotificationType.TASK)
                    .orElse(DEFAULT_SUMMARY_TIME);

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
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.SCHEDULER_REGISTER_FAIL);
        }
    }

    /**
     * 매일 22:00 미완료 리마인드 등록
     */
    public void registerDailyIncompleteReminder(Long memberId) {
        try {
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
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.SCHEDULER_REGISTER_FAIL);
        }
    }
}