package com.zero.cohousesever.post.dto.post;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.type.PostColor;
import com.zero.cohousesever.post.type.PostType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 목록 카드(UI) 표시용 경량 DTO.
 * - title, preview(본문 앞부분 100자), 작성자/색상 요약
 * - 리스트/피드 화면에서 사용
 */
@Getter
@Builder
public class PostSummaryResponse {

    private Long id;
    private PostType type;
    private String title;
    private String preview;
    private Long groupId;
    private Long memberId;
    private PostColor color;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> DTO 변환 (개행/공백 정규화 후 100자 발췌)
     */
    public static PostSummaryResponse from(Post post) {
        String content = post.getContent() == null ? "" : post.getContent();
        String normalized = content.replaceAll("\\s+", " ");
        String preview = normalized.length() <= 100 ? normalized : normalized.substring(0, 100) + "...";

        return PostSummaryResponse.builder()
                .id(post.getId())
                .type(post.getType())
                .title(post.getTitle())
                .preview(preview)
                .groupId(post.getGroupId())
                .memberId(post.getMemberId())
                .color(post.getColor())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}