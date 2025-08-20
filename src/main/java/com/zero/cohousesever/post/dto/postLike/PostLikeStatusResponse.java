package com.zero.cohousesever.post.dto.postLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좋아요 상태(여부) 조회 응답 DTO.
 * - 내가 해당 게시글을 좋아요 했는지 여부만 반환
 * - 하트 UI 토글 상태 결정에 사용
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeStatusResponse {
    private Long postId;
    private boolean isLiked;
}