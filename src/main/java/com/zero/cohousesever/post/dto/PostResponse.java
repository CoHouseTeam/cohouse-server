package com.zero.cohousesever.post.dto;

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
}