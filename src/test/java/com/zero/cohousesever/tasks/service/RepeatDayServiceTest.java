package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepeatDayServiceTest {

  @Mock RepeatDayRepository repeatDayRepository;
  @Mock TaskTemplateRepository taskTemplateRepository;
  @InjectMocks RepeatDayService repeatDayService;

  private TaskTemplate templateWithId(long id) {
    TaskTemplate t = TaskTemplate.builder().groupId(1L).category("CLEANING").build();
    ReflectionTestUtils.setField(t, "id", id);
    return t;
  }

  @Test
  @DisplayName("반복요일 생성 - 중복이면 기존 반환, 아니면 저장")
  void addRepeatDay_dedupAndSave() {
    long templateId = 10L;
    TaskTemplate tmpl = templateWithId(templateId);
    when(taskTemplateRepository.findById(templateId)).thenReturn(Optional.of(tmpl));

    RepeatDayRequest req = new RepeatDayRequest();
    req.setDayOfWeek("MONDAY");

    // 1) 중복 없는 경우 → save 호출
    when(repeatDayRepository.findByTaskTemplate_IdAndDayOfWeek(templateId, DayOfWeek.MONDAY))
        .thenReturn(Optional.empty());
    RepeatDay saved = RepeatDay.builder().taskTemplate(tmpl).dayOfWeek(DayOfWeek.MONDAY).build();
    ReflectionTestUtils.setField(saved, "id", 100L);
    when(repeatDayRepository.save(any(RepeatDay.class))).thenReturn(saved);

    RepeatDayResponse r1 = repeatDayService.addRepeatDay(templateId, req);
    assertThat(r1.getRepeatDayId()).isEqualTo(100L);
    verify(repeatDayRepository).save(any(RepeatDay.class));

    // 2) 중복인 경우 → 기존 반환, save 호출 안 함
    when(repeatDayRepository.findByTaskTemplate_IdAndDayOfWeek(templateId, DayOfWeek.MONDAY))
        .thenReturn(Optional.of(saved));
    RepeatDayResponse r2 = repeatDayService.addRepeatDay(templateId, req);
    assertThat(r2.getRepeatDayId()).isEqualTo(100L);
    verify(repeatDayRepository, times(1)).save(any(RepeatDay.class)); // 총 1번
  }

  @Test
  @DisplayName("반복요일 조회 - 일요일이 먼저")
  void getRepeatDays_sortedSundayFirst() {
    long templateId = 10L;
    TaskTemplate tmpl = templateWithId(templateId);

    RepeatDay mon = RepeatDay.builder().taskTemplate(tmpl).dayOfWeek(DayOfWeek.MONDAY).build();
    RepeatDay sun = RepeatDay.builder().taskTemplate(tmpl).dayOfWeek(DayOfWeek.SUNDAY).build();
    ReflectionTestUtils.setField(mon, "id", 1L);
    ReflectionTestUtils.setField(sun, "id", 2L);

    when(repeatDayRepository.findByTaskTemplate_Id(templateId))
        .thenReturn(List.of(mon, sun));

    var list = repeatDayService.getRepeatDaysByTemplateId(templateId);
    assertThat(list).hasSize(2);
    assertThat(list.get(0).getDayOfWeek()).isEqualTo("SUNDAY");
    assertThat(list.get(1).getDayOfWeek()).isEqualTo("MONDAY");
  }

  @Test
  @DisplayName("반복요일 삭제 - 소속 템플릿 검증 포함")
  void deleteRepeatDay_withOwnershipCheck() {
    long templateId = 10L;

    // 삭제 성공
    when(repeatDayRepository.deleteByIdAndTaskTemplate_Id(5L, templateId)).thenReturn(1L);
    repeatDayService.deleteRepeatDay(templateId, 5L);

    // 삭제 실패(소속 불일치/존재X)
    when(repeatDayRepository.deleteByIdAndTaskTemplate_Id(6L, templateId)).thenReturn(0L);
    assertThatThrownBy(() -> repeatDayService.deleteRepeatDay(templateId, 6L))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.REPEAT_DAY_NOT_FOUND);
  }
}
