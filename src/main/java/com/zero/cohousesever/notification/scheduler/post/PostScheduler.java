// src/main/java/com/zero/cohousesever/notification/scheduler/post/PostScheduler.java
package com.zero.cohousesever.notification.scheduler.post;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.scheduler.post.job.PostAnnouncementJob;
import com.zero.cohousesever.notification.scheduler.support.PreferredTimeProvider;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class PostScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime DEFAULT_POST_TIME = LocalTime.of(22, 0);

    private final Scheduler scheduler;
    private final PreferredTimeProvider timeProvider;

    /**
     * 공지 1회 발송 예약
     */
    public void scheduleAnnouncementOnce(Long memberId, Long postId) {
        try {
            LocalTime fireAt = timeProvider
                    .getPreferredTime(memberId, NotificationType.ANNOUNCEMENT)
                    .orElse(DEFAULT_POST_TIME);

            LocalDateTime when = nextFireDateTime(fireAt, KST);

            JobDetail job = JobBuilder.newJob(PostAnnouncementJob.class)
                    .withIdentity("post:ann:" + postId + ":" + memberId)
                    .usingJobData("memberId", memberId)
                    .usingJobData("postId", postId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("post:ann:trg:" + postId + ":" + memberId)
                    .startAt(toDate(when, KST))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.SCHEDULER_REGISTER_FAIL);
        }
    }

    private static LocalDateTime nextFireDateTime(LocalTime time, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalDateTime candidate = LocalDateTime.of(today, time);
        return candidate.isAfter(LocalDateTime.now(zone))
                ? candidate
                : LocalDateTime.of(today.plusDays(1), time);
    }

    private static Date toDate(LocalDateTime dt, ZoneId zone) {
        return Date.from(dt.atZone(zone).toInstant());
    }
}