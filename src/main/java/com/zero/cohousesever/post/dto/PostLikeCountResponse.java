package com.zero.cohousesever.board.dto.like;

import lombok.*;

/**
 * 특정 게시글의 좋아요 개수만 응답.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeCountResponse {
    private Long postId;
    private long count;
}