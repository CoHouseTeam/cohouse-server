package com.zero.cohousesever.post.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 좋아요를 누른 사용자 요약 정보.
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