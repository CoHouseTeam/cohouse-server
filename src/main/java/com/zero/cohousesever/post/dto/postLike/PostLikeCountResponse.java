package com.zero.cohousesever.post.dto.postLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 개수 조회 응답 DTO.
 * - 특정 게시글의 총 좋아요 수를 제공
 * - 하트 아이콘 옆 숫자 표시에 사용
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeCountResponse {
    private Long postId;
    private long count;
}