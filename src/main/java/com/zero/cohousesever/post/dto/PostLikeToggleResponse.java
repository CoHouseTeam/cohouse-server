package com.zero.cohousesever.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 토글 이후 UI를 즉시 갱신하기 위한 응답 DTO
 *  - isLiked: 최종 하트 상태 (true=빨간 하트, false=빈 하트)
 *  - likeCount: 토글 직후 최신 좋아요 수 (하트 옆 숫자)
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeToggleResponse {

    private final Long postId;
    private final boolean isLiked;
    private final long likeCount;
}