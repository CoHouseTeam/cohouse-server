package com.zero.cohousesever.post.event;

import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
import com.zero.cohousesever.notification.scheduler.post.PostScheduler;
import com.zero.cohousesever.notification.scheduler.support.AppPresenceChecker;
import com.zero.cohousesever.notification.service.NotificationService;
import com.zero.cohousesever.notification.service.NotificationSettingService;
import com.zero.cohousesever.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final GroupMemberRepository groupMemberRepository;
    private final NotificationSettingService notificationSettingService;
    private final NotificationService notificationService;
    private final PostScheduler postScheduler;
    private final AppPresenceChecker presenceChecker; // 네가 올린 인터페이스 그대로 사용

    @EventListener
    public void onAnnouncementCreated(PostAnnouncementCreatedEvent event) {
        Long groupId = event.getGroupId();
        Long postId = event.getPostId();

        // 1) 수신자: 그룹 내 ACTIVE 멤버만
        List<GroupMember> gmList =
                groupMemberRepository.findAllByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
        if (gmList == null || gmList.isEmpty()) {
            log.info("[PostEvent] no receivers for groupId={}", groupId);
            return;
        }

        // 2) 멤버별 처리
        for (GroupMember gm : gmList) {
            Long memberId = gm.getMember().getId();

            // 2-1) 공지 알림이 꺼져 있으면 스킵
            boolean enabled = notificationSettingService.isEnabled(memberId, NotificationType.ANNOUNCEMENT);
            if (!enabled) {
                log.debug("[PostEvent] announcement OFF, skip. memberId={}", memberId);
                continue;
            }

            // 2-2) 접속 중이면 즉시 발송
            boolean active = presenceChecker.isActive(memberId);
            if (active) {
                notificationService.create(
                        memberId,
                        NotificationCreateRequest.builder()
                                .type(NotificationType.ANNOUNCEMENT)
                                .title("새 공지")
                                .content("새 공지가 등록되었습니다. (postId=" + postId + ")")
                                .build(),
                        true  // isAppActive = true : 즉시 발송 정책
                );
                continue;
            }

            // 2-3) 미접속이면 사용자 선호 시각(없으면 22:00)에 1회 예약
            postScheduler.scheduleAnnouncementOnce(memberId, postId);
        }
    }
}