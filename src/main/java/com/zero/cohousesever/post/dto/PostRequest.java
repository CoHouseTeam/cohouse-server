package com.zero.cohousesever.post.dto;

import com.zero.cohousesever.post.type.PostType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    private Long groupId;
    private Long memberId;
    private PostType type;
    private String title;
    private String content;
}
