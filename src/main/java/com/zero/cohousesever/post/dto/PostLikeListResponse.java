package com.zero.cohousesever.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 특정 게시글을 좋아요한 사용자 목록 응답.
 * - totalCount: 총 인원 수
 * - likers: 사용자 목록 (최소 memberId)
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