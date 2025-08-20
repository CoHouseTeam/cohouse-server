package com.zero.cohousesever.post.dto.post;

import com.zero.cohousesever.post.type.PostColor;
import com.zero.cohousesever.post.type.PostType;
import lombok.*;

/**
 * 게시글 생성 요청 DTO.
 * - 사용자가 게시글 작성 시 전달하는 입력 데이터
 * - title, content, color, type 등 필드 포함
 */
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
    private PostColor color;
}
