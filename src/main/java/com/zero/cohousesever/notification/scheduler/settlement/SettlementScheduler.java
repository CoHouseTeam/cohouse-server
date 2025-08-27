package com.zero.cohousesever.notification.scheduler.settlement;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.scheduler.settlement.job.SettlementReminderJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 * 정산 스케줄러:
 * - 등록 즉시 알림은 도메인 서비스에서 즉시 NotificationService.create 호출 권장
 * - 추가로 미송금 시 "다음날부터" 매일 22:00 리마인드 예약
 */
@Service
@RequiredArgsConstructor
public class SettlementScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAILY_REMINDER_HOUR = 22;

    private final Scheduler scheduler;

    /**
     * 미송금 리마인드(매일 22:00), 시작은 내일 22:00
     */
    public void scheduleDailyUnpaidReminder(Long memberId, Long settlementId) {
        try {
            JobDetail job = JobBuilder.newJob(SettlementReminderJob.class)
                    .withIdentity("settle:rem:" + settlementId + ":" + memberId)
                    .usingJobData("memberId", memberId)
                    .usingJobData("settlementId", settlementId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("settle:rem:trg:" + settlementId + ":" + memberId)
                    .withSchedule(CronScheduleBuilder
                            .dailyAtHourAndMinute(DAILY_REMINDER_HOUR, 0)
                            .inTimeZone(TimeZone.getTimeZone(KST)))
                    .startAt(tomorrowAt(DAILY_REMINDER_HOUR, 0, KST))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.SCHEDULER_REGISTER_FAIL);
        }
    }

    private static Date tomorrowAt(int hour, int minute, ZoneId zone) {
        LocalDateTime dt = LocalDate.now(zone).plusDays(1).atTime(hour, minute);
        return Date.from(dt.atZone(zone).toInstant());
    }
}