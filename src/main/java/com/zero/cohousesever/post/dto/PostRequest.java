package com.zero.cohousesever.post.dto;

import com.zero.cohousesever.post.type.PostType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    private Long groupMemberId;
    private String postTitle;
    private String postContent;
    private PostType postType;
}
