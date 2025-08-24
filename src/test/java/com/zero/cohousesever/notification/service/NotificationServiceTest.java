package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.dto.NotificationResponse;
import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.repository.NotificationRepository;
import com.zero.cohousesever.notification.type.NotificationStatus;
import com.zero.cohousesever.notification.type.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationService 단위 테스트.
 * - Repository를 Mock 처리하여 서비스 계층의 분기/매핑/위임을 검증합니다.
 */
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification n1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 알림 엔티티 (공지 알림)
        n1 = Notification.builder()
                .type(NotificationType.ANNOUNCEMENT)
                .title("공지 알림")
                .content("새로운 공지가 등록되었습니다.")
                .isRead(false)
                .status(NotificationStatus.ACTIVE)
                .build();
        n1.setId(1L); // 테스트 식별자
    }

    @Test
    @DisplayName("알림 목록 조회 - 필터 없음(전체)")
    void getNotifications_noFilters_returnsList() {
        // given
        when(notificationRepository.findByMember(
                eq(10L),
                any(LocalDateTime.class),
                eq(NotificationStatus.ACTIVE),
                isNull(),   // type 없음
                isNull()    // read 없음
        )).thenReturn(List.of(n1));

        // when
        var list = notificationService.getNotifications(10L, null, null);

        // then
        assertThat(list).hasSize(1);
        NotificationResponse res = list.get(0);
        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getType()).isEqualTo(NotificationType.ANNOUNCEMENT);
        assertThat(res.getTitle()).isEqualTo("공지 알림");

        verify(notificationRepository, times(1)).findByMember(
                eq(10L), any(LocalDateTime.class), eq(NotificationStatus.ACTIVE), isNull(), isNull()
        );
    }

    @Test
    @DisplayName("알림 목록 조회 - 타입 필터(ANNOUNCEMENT) 적용")
    void getNotifications_withType_returnsFilteredList() {
        // given
        when(notificationRepository.findByMember(
                eq(10L),
                any(LocalDateTime.class),
                eq(NotificationStatus.ACTIVE),
                eq(NotificationType.ANNOUNCEMENT), // type=ANNOUNCEMENT
                isNull()
        )).thenReturn(List.of(n1));

        // when
        var list = notificationService.getNotifications(10L, NotificationType.ANNOUNCEMENT, null);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getType()).isEqualTo(NotificationType.ANNOUNCEMENT);

        verify(notificationRepository, times(1)).findByMember(
                eq(10L), any(LocalDateTime.class), eq(NotificationStatus.ACTIVE), eq(NotificationType.ANNOUNCEMENT), isNull()
        );
    }

    @Test
    @DisplayName("알림 목록 조회 - 읽음 여부 필터(false) 적용")
    void getNotifications_withReadFilter_returnsFilteredList() {
        // given
        when(notificationRepository.findByMember(
                eq(10L),
                any(LocalDateTime.class),
                eq(NotificationStatus.ACTIVE),
                isNull(),
                eq(false)
        )).thenReturn(List.of(n1));

        // when
        var list = notificationService.getNotifications(10L, null, false);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).isRead()).isFalse();

        verify(notificationRepository, times(1)).findByMember(
                eq(10L), any(LocalDateTime.class), eq(NotificationStatus.ACTIVE), isNull(), eq(false)
        );
    }

    @Test
    @DisplayName("전체 삭제 - 논리 삭제 수행 및 영향 행 수 반환")
    void softDeleteAll_updatesRows() {
        // given
        when(notificationRepository.softDeleteAllByMember(
                eq(10L), eq(NotificationStatus.DELETED), any(LocalDateTime.class)
        )).thenReturn(5);

        // when
        notificationService.softDeleteAll(10L);

        // then
        verify(notificationRepository, times(1)).softDeleteAllByMember(
                eq(10L), eq(NotificationStatus.DELETED), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("보관기간 경과분 일괄 삭제 - 영향 행 수 반환")
    void softDeleteOutdated_returnsAffectedRows() {
        // given
        when(notificationRepository.softDeleteOlderThan(
                any(LocalDateTime.class), eq(NotificationStatus.DELETED), any(LocalDateTime.class)
        )).thenReturn(12);

        // when
        int affected = notificationService.softDeleteOutdated();

        // then
        assertThat(affected).isEqualTo(12);
        verify(notificationRepository, times(1)).softDeleteOlderThan(
                any(LocalDateTime.class), eq(NotificationStatus.DELETED), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("읽음 처리 성공 - 미읽음 → 읽음")
    void markRead_success() {
        when(notificationRepository.markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any()))
                .thenReturn(1);

        assertDoesNotThrow(() -> notificationService.markAsRead(10L, 100L));

        verify(notificationRepository, times(1))
                .markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any());
        verify(notificationRepository, never())
                .findReadFlagForActiveMember(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("읽음 처리 멱등 - 이미 읽음")
    void markRead_idempotent_alreadyRead() {
        when(notificationRepository.markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any()))
                .thenReturn(0);
        when(notificationRepository.findReadFlagForActiveMember(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE)))
                .thenReturn(Boolean.TRUE);

        assertDoesNotThrow(() -> notificationService.markAsRead(10L, 100L));

        verify(notificationRepository, times(1))
                .findReadFlagForActiveMember(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE));
    }

    @Test
    @DisplayName("읽음 처리 실패 - 대상 없음/권한 없음/삭제됨")
    void markAsRead_notFoundOrForbidden() {
        // given: 업데이트 0건 + 존재하지 않음(null)
        when(notificationRepository.markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any()))
                .thenReturn(0);
        when(notificationRepository.findReadFlagForActiveMember(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE)))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(10L, 100L))
                .isInstanceOf(CustomException.class)
                // ErrorCode의 메시지로 검증 (가장 안전)
                .hasMessageContaining(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());

        verify(notificationRepository, times(1))
                .findReadFlagForActiveMember(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE));
    }
}