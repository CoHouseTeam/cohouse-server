package com.zero.cohousesever.post.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *  공지사항 목록 요약 DTO (제목 + 생성일시)
 */
@Getter
@Builder
@AllArgsConstructor
public class AnnouncementSummaryResponse {
    private Long id;
    private String title;
    private LocalDate date;
}