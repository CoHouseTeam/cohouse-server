package com.zero.cohousesever.notification.scheduler.settlement;

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
 * 정산 스케줄러
 * - “미송금”이면 내일부터 매일 22:00에 리마인드 잡 실행
 */
@Service
@RequiredArgsConstructor
public class SettlementScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final Scheduler scheduler;

    /**
     * 내일부터 매일 22:00에 SettlementReminderJob 실행
     */
    public void scheduleDailyUnpaidReminder(Long memberId, Long settlementId) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(SettlementReminderJob.class)
                .withIdentity("settle:rem:" + settlementId + ":" + memberId)
                .usingJobData("memberId", memberId)
                .usingJobData("settlementId", settlementId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("settle:rem:trg:" + settlementId + ":" + memberId)
                .withSchedule(CronScheduleBuilder
                        .dailyAtHourAndMinute(22, 0)
                        .inTimeZone(TimeZone.getTimeZone(KST)))
                .startAt(tomorrowAt(22, 0, KST))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    /**
     * 더 이상 필요 없을 때(송금 완료 등) 예약 제거
     */
    public void cancelDailyUnpaidReminder(Long memberId, Long settlementId) throws SchedulerException {
        scheduler.deleteJob(new JobKey("settle:rem:" + settlementId + ":" + memberId));
    }

    private static Date tomorrowAt(int hour, int minute, ZoneId zone) {
        LocalDateTime dt = LocalDate.now(zone).plusDays(1).atTime(hour, minute);
        return Date.from(dt.atZone(zone).toInstant());
    }
}