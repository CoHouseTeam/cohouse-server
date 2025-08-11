package com.zero.cohousesever.post.dto;

import com.zero.cohousesever.post.type.PostType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long PostId;
    private String postTitle;
    private String postContent;
    private PostType postType;
    private Long groupMemberId;
    private LocalDateTime createdAt;
    private int likeCount;
}