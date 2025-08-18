package com.zero.cohousesever.post.dto;

import lombok.*;

import java.util.List;

/**
 * 특정 게시글을 좋아요한 사용자 목록 응답.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikeListResponse {
    private Long postId;
    private long totalCount;
    private List<PostLikerDto> likers;
}