package com.zero.cohousesever.post.dto;

import lombok.*;

/**
 * 좋아요 여부와 개수를 동시에 응답.
 * 프론트에서 하트 색상 변경과 개수 업데이트에 사용.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeSimpleResponse {
    private Long postId;
    private boolean isLiked;
    private long count;
}