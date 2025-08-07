package com.zero.cohousesever.board.dto;

import com.zero.cohousesever.board.type.PostType;
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