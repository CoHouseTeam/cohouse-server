package com.zero.cohousesever.post.dto.post;

import com.zero.cohousesever.post.entity.Post;
import com.zero.cohousesever.post.type.PostType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 목록 카드(UI) 표시용 경량 DTO.
 * - preview: 본문 앞부분 100자 발췌 (검색이 없어도 카드 요약용으로 유지)
 * - likeCount 기능 연동 전까지 0
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 좋아요 기능 구현 후 실제 값으로 교체 예정
    private Integer likeCount;

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
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(0)      // TODO: 좋아요 기능 연동 시 실제 값으로 변경
                .build();
    }
}