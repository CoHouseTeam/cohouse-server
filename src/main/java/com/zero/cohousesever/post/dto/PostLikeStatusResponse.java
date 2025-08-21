package com.zero.cohousesever.post.dto;

import lombok.*;

/**
 * 특정 게시글에 대해 현재 사용자가 좋아요를 눌렀는지 여부 응답.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeStatusResponse {
    private Long postId;
    private boolean isLiked;
}