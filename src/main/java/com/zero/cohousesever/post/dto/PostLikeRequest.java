package com.zero.cohousesever.post.dto;

import lombok.*;

/**
 * 게시글 좋아요 요청 DTO.
 * isLiked: true면 좋아요 추가 요청, false면 좋아요 취소 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeRequest {

    private Long memberId;
    private boolean isLiked;
}