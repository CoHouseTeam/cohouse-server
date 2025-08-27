package com.zero.cohousesever.notification.scheduler.post.job;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.scheduler.support.AppPresenceChecker;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Component;

/** 공지 알림 1회 발송 잡 */
@Component
@RequiredArgsConstructor
public class PostAnnouncementJob implements Job {

    private final NotificationService notificationService;
    private final AppPresenceChecker presenceChecker;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap m = context.getMergedJobDataMap();
        Long memberId = m.getLong("memberId");
        Long postId   = m.getLong("postId");

        boolean active = presenceChecker.isActive(memberId);

        notificationService.create(
                memberId,
                new NotificationCreateRequest(
                        NotificationType.ANNOUNCEMENT,
                        "새 공지",
                        "새 공지가 등록되었습니다. (postId=" + postId + ")"
                ),
                active // 잡 실행 시에도 접속 중이면 즉시 정책
        );
    }
}