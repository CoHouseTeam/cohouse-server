package com.zero.cohousesever.post.dto.postLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 설정/해제 결과 응답 DTO.
 * - 토글 수행 후의 최종 상태(isLiked)와 최신 좋아요 수(likeCount) 반환
 * - 동시성/멱등 처리 결과와 무관하게 최종 상태 기준으로 응답
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeToggleResponse {

    private Long postId;
    private boolean isLiked;
    private long likeCount;
}