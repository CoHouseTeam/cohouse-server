package com.zero.cohousesever.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 게시글의 좋아요 개수만 응답.
 * - postId: 어떤 게시글에 대한 개수인지 식별
 * - count: 좋아요 총 개수
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeCountResponse {
    private Long postId;
    private long count;
}