package com.zero.cohousesever.post.dto;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.type.PostType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private Long groupId;
    private Long memberId;
    private PostType type;
    private String title;
    private String content;
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
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}