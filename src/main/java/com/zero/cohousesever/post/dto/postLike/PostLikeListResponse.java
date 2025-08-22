package com.zero.cohousesever.post.dto.postLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 좋아요한 사용자 목록 응답 DTO.
 * - 특정 게시글을 좋아요한 사용자들의 요약 리스트를 전달
 * - postId, totalCount, likers(프로필/이름) 포함
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikeListResponse {
    private Long postId;
    private long totalCount;
    private List<PostLikerDto> likers;
}