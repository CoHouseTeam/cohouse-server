package com.zero.cohousesever.board.dto;

import com.zero.cohousesever.board.type.PostType;
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
