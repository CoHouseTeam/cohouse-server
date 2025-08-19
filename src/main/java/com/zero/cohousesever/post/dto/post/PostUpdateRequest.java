package com.zero.cohousesever.post.dto.post;

import com.zero.cohousesever.post.type.PostType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {
    private String title;
    private String content;
    private PostType type;
}
