package com.zero.cohousesever.notification.service;

import com.zero.cohousesever.notification.dto.NotificationCreateRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  private Notification announcement; // 공지 알림 엔티티

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // 공지 알림 엔티티(필수 필드만) - getter 사용 전제
    announcement = Notification.builder()
            .type(NotificationType.ANNOUNCEMENT)
            .title("공지 알림")
            .content("새로운 공지가 등록되었습니다.")
            .isRead(false)
            .status(NotificationStatus.ACTIVE)
            .build();
    ReflectionTestUtils.setField(announcement, "id", 1L);
    ReflectionTestUtils.setField(announcement, "createdAt", LocalDateTime.now());
  }

  @Test
  @DisplayName("공지 알림 생성 - 저장 성공")
  void create_announcement_success() {
    // given
    Long memberId = 10L;
    NotificationCreateRequest req = new NotificationCreateRequest(
            NotificationType.ANNOUNCEMENT,
            "새 공지",
            "공지 내용을 확인해 주세요."
    );

    when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
      Notification n = invocation.getArgument(0);
      ReflectionTestUtils.setField(n, "id", 777L);
      ReflectionTestUtils.setField(n, "createdAt", LocalDateTime.now());
      return n;
    });

    // when
    NotificationResponse res = notificationService.create(memberId, req);

    // then (getter 기반)
    assertThat(res.getId()).isEqualTo(777L);
    assertThat(res.getType()).isEqualTo(NotificationType.ANNOUNCEMENT);
    assertThat(res.getTitle()).isEqualTo("새 공지");
    assertThat(res.getContent()).isEqualTo("공지 내용을 확인해 주세요.");
    assertThat(res.isRead()).isFalse();

    verify(notificationRepository, times(1)).save(any(Notification.class));
  }

  @Test
  @DisplayName("알림 목록 조회 - 타입 필터(ANNOUNCEMENT) 적용")
  void getNotifications_filterByAnnouncement() {
    // given
    when(notificationRepository.findByMember(
            eq(10L),
            any(LocalDateTime.class),
            eq(NotificationStatus.ACTIVE),
            eq(NotificationType.ANNOUNCEMENT),
            isNull()
    )).thenReturn(List.of(announcement));

    // when
    var list = notificationService.getNotifications(10L, NotificationType.ANNOUNCEMENT, null);

    // then
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getType()).isEqualTo(NotificationType.ANNOUNCEMENT);
    assertThat(list.get(0).getTitle()).isEqualTo("공지 알림");

    verify(notificationRepository, times(1)).findByMember(
            eq(10L), any(LocalDateTime.class), eq(NotificationStatus.ACTIVE),
            eq(NotificationType.ANNOUNCEMENT), isNull()
    );
  }

  @Test
  @DisplayName("읽음 처리 성공 - 미읽음 → 읽음")
  void markAsRead_success() {
    // given: 업데이트 1건 성공
    when(notificationRepository.markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any()))
            .thenReturn(1);

    // when & then
    assertDoesNotThrow(() -> notificationService.markAsRead(10L, 100L));

    verify(notificationRepository, times(1))
            .markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any());
    verify(notificationRepository, never())
            .findReadFlagForActiveMember(anyLong(), anyLong(), any());
  }

  @Test
  @DisplayName("읽음 처리 멱등 - 이미 읽음")
  void markAsRead_idempotent_whenAlreadyRead() {
    // given: 업데이트 0건 + 이미 읽음(true)
    when(notificationRepository.markRead(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE), any()))
            .thenReturn(0);
    when(notificationRepository.findReadFlagForActiveMember(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE)))
            .thenReturn(Boolean.TRUE);

    // when & then
    assertDoesNotThrow(() -> notificationService.markAsRead(10L, 100L));

    verify(notificationRepository, times(1))
            .findReadFlagForActiveMember(eq(100L), eq(10L), eq(NotificationStatus.ACTIVE));
  }
}