package com.zero.cohousesever.post.dto.post;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.type.PostColor;
import com.zero.cohousesever.post.type.PostStatus;
import com.zero.cohousesever.post.type.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 상세 응답 DTO.
 * - 게시글 단건 상세 조회 결과를 내려줄 때 사용
 * - 작성자, 그룹, 본문, 색상, 상태 등 전체 정보 포함
 */
@Getter
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private Long groupId;
    private Long memberId;
    private PostType type;
    private String title;
    private String content;
    private PostStatus status;
    private PostColor color;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .groupId(post.getGroupId())
                .memberId(post.getMemberId())
                .type(post.getType())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .color(post.getColor())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}