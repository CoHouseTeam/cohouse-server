package com.zero.cohousesever.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 목록 데이터 + 페이지 메타 정보 래퍼 DTO
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