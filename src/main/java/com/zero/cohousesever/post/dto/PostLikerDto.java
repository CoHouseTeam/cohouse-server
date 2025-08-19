package com.zero.cohousesever.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요한 사용자 정보 DTO
 * - memberId: 사용자 PK
 * - displayName: 사용자 표시명(닉네임/이름)
 * - avatarUrl: 프로필 이미지 URL
 */
@Getter
@AllArgsConstructor
@Builder
public class PostLikerDto {

    private final Long memberId;
    private final String displayName;
    private final String avatarUrl;
}