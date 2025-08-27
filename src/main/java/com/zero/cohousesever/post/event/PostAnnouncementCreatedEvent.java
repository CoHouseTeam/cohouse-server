package com.zero.cohousesever.post.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 게시판 공지 생성 이벤트
 * - postId: 게시물 ID
 * - receiverMemberIds: 수신 대상자(멤버 ID 목록)
 */
@Getter
@Builder
@AllArgsConstructor
public class PostAnnouncementCreatedEvent {

    private final Long postId;
    private final Long groupId;
}