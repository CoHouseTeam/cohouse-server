package com.zero.cohousesever.post.dto.post;

import com.zero.cohousesever.post.type.PostColor;
import com.zero.cohousesever.post.type.PostType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 수정 요청 DTO.
 * - 사용자가 기존 게시글을 수정할 때 전달
 * - title, content, color 등 갱신 대상 필드 포함
 */
@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {

    private String title;
    private String content;
    private PostType type;
    private PostColor color;
}
