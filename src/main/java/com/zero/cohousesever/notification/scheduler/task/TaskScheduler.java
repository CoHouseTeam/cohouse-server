package com.zero.cohousesever.notification.scheduler.task;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.scheduler.support.PreferredTimeProvider;
import com.zero.cohousesever.notification.scheduler.task.job.TaskDailySummaryJob;
import com.zero.cohousesever.notification.scheduler.task.job.TaskIncompleteReminderJob;
import com.zero.cohousesever.notification.type.NotificationType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskScheduler {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final LocalTime DEFAULT_SUMMARY_TIME = LocalTime.of(8, 0);
  private static final LocalTime INCOMPLETE_REMINDER_TIME = LocalTime.of(22, 0);

  private final Scheduler scheduler;
  private final PreferredTimeProvider timeProvider;

  /**
   * 오늘 날짜 키 (yyyyMMdd)
   */
  private String todayKey() {
    return LocalDate.now(KST).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
  }

  private Date kstDateTime(LocalTime time) {
    // 알림 시간이 이미 지났으면 지금+5초로 당겨서 즉시 보내기
    ZonedDateTime now = ZonedDateTime.now(KST);
    ZonedDateTime runAt = LocalDate.now(KST).atTime(time).atZone(KST);
    if (!runAt.isAfter(now)) {
      runAt = now.plusSeconds(5);
    }
    return Date.from(runAt.toInstant());
  }

  // =============== 오늘 '요약' 단발 스케줄 ===============

  /**
   * (조건) 오늘 할일이 있을 때만, 사용자 지정시각(없으면 08:00)에 1회 실행
   */
  public void scheduleTodaySummaryIfNeeded(Long memberId) {
    try {
      LocalTime time = timeProvider
          .getPreferredTime(memberId, NotificationType.TASK)
          .orElse(DEFAULT_SUMMARY_TIME);

      String jn = "task:sum:" + memberId + ":" + todayKey();
      String tn = "task:sum:trg:" + memberId + ":" + todayKey();

      JobDetail job = JobBuilder.newJob(TaskDailySummaryJob.class)
          .withIdentity(jn)
          .usingJobData("memberId", memberId)
          .usingJobData("hhmm", time.toString())
          .storeDurably(false)
          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(tn)
          .startAt(kstDateTime(time))  // 단발
          .withSchedule(SimpleScheduleBuilder.simpleSchedule()
              .withMisfireHandlingInstructionFireNow())
          .build();

      // 이미 있으면 시간만 갱신
      if (scheduler.checkExists(new TriggerKey(tn))) {
        scheduler.rescheduleJob(new TriggerKey(tn), trigger);
      } else if (scheduler.checkExists(new JobKey(jn))) {
        scheduler.deleteJob(new JobKey(jn));
        scheduler.scheduleJob(job, trigger);
      } else {
        scheduler.scheduleJob(job, trigger);
      }
    } catch (SchedulerException e) {
      throw new CustomException(ErrorCode.SCHEDULER_REGISTER_FAIL);
    }
  }

  // =============== 오늘 '미완료 리마인드' 단발 스케줄 ===============

  /**
   * (조건) 오늘 할일이 있을 때만 22:00에 1회 실행
   */
  public void scheduleTonightIncompleteReminder(Long memberId) {
    try {
      String jn = "task:inc:" + memberId + ":" + todayKey();
      String tn = "task:inc:trg:" + memberId + ":" + todayKey();

      JobDetail job = JobBuilder.newJob(TaskIncompleteReminderJob.class)
          .withIdentity(jn)
          .usingJobData("memberId", memberId)
          .storeDurably(false)
          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(tn)
          .startAt(kstDateTime(INCOMPLETE_REMINDER_TIME)) // 단발
          .withSchedule(SimpleScheduleBuilder.simpleSchedule()
              .withMisfireHandlingInstructionFireNow())
          .build();

      if (scheduler.checkExists(new TriggerKey(tn))) {
        scheduler.rescheduleJob(new TriggerKey(tn), trigger);
      } else if (scheduler.checkExists(new JobKey(jn))) {
        scheduler.deleteJob(new JobKey(jn));
        scheduler.scheduleJob(job, trigger);
      } else {
        scheduler.scheduleJob(job, trigger);
      }
    } catch (SchedulerException e) {
      throw new CustomException(ErrorCode.SCHEDULER_REGISTER_FAIL);
    }
  }

  /**
   * 오늘 22:00 리마인드 예약 제거 (모두 완료 시 호출)
   */
  public void cancelTonightIncompleteReminder(Long memberId) {
    try {
      String jn = "task:inc:" + memberId + ":" + todayKey();
      scheduler.deleteJob(new JobKey(jn));
    } catch (SchedulerException e) {
      throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
  }
}
