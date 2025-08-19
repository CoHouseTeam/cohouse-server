package com.zero.cohousesever.post.dto.postLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상태 조회 응답
 * -특정 게시글에 대해 현재 사용자가 좋아요를 눌렀는지 여부 응답.
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeStatusResponse {
    private Long postId;
    private boolean isLiked;
}