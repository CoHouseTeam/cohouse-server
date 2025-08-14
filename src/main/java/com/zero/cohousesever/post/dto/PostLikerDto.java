package com.zero.cohousesever.post.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 좋아요를 누른 사용자 요약 정보.
 * - 닉네임/프로필 이미지는 프로젝트 상황에 맞춰 필드 추가하세요.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLikerDto {
    private Long memberId;
    private String name;
    private LocalDateTime likedAt;
}