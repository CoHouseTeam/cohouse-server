package com.zero.cohousesever.post.dto.postLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 사용자 요약 DTO.
 * - 목록 아이템 단위: memberId, displayName, avatarUrl
 * - 상세 정보가 필요 없을 때 최소 정보만 전달
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikerDto {

    private Long memberId;
    private String displayName;
    private String avatarUrl;
}