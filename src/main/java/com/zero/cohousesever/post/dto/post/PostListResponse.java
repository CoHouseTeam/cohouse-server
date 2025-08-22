package com.zero.cohousesever.post.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 게시글 목록 응답 DTO.
 * - 다건 조회 시 각 게시글 요약 정보를 담아 전달
 * - 리스트/피드 화면에서 사용
 */
@Getter
@Builder
public class PostListResponse<T> {

    private List<T> content;
    private int page;           // 0-base
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PostListResponse<T> from(List<T> content,
                                               int page, int size,
                                               long totalElements, int totalPages,
                                               boolean last) {
        return PostListResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .build();
    }
}