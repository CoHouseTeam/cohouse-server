package com.zero.cohousesever.notification.scheduler.post;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.scheduler.post.job.PostAnnouncementJob;
import com.zero.cohousesever.notification.scheduler.support.AppPresenceChecker;
import com.zero.cohousesever.notification.scheduler.support.PreferredTimeProvider;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;

/**
 * 게시판(공지) 스케줄러:
 * - 사용자 지정시각(없으면 22:00)에 공지 1회 발송 예약
 * - 앱 접속 중이면 즉시 발송
 */
@Service
@RequiredArgsConstructor
public class PostScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime DEFAULT_POST_TIME = LocalTime.of(22, 0);

    private final Scheduler scheduler;
    private final PreferredTimeProvider timeProvider;
    private final AppPresenceChecker presenceChecker;
    private final NotificationService notificationService;

    /** 공지 1회 발송 예약 (접속중이면 즉시 발송) */
    public void scheduleAnnouncementOnce(Long memberId, Long postId) throws SchedulerException {
        // 접속 중이면 즉시
        boolean active = presenceChecker.isActive(memberId);
        if (active) {
            notificationService.create(
                    memberId,
                    new NotificationCreateRequest(
                            NotificationType.ANNOUNCEMENT,
                            "새 공지",
                            "새 공지가 등록되었습니다. (postId=" + postId + ")"
                    ),
                    true // 앱 접속 중
            );
            return;
        }

        // 접속중이 아니라면 사용자의 선호 시각(없으면 22:00)에 1회 발송
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
    }

    private static LocalDateTime nextFireDateTime(LocalTime time, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalDateTime candidate = LocalDateTime.of(today, time);
        if (candidate.isAfter(LocalDateTime.now(zone))) return candidate;
        return LocalDateTime.of(today.plusDays(1), time);
    }

    private static Date toDate(LocalDateTime dt, ZoneId zone) {
        return Date.from(dt.atZone(zone).toInstant());
    }
}