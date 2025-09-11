package com.zero.cohousesever.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.task.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.task.dto.template.TaskTemplateUpdateRequest;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import com.zero.cohousesever.task.repository.TaskTemplateRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskTemplateServiceTest {

  @Mock
  TaskTemplateRepository taskTemplateRepository;
  @Mock
  RepeatDayService repeatDayService;

  @InjectMocks
  TaskTemplateService taskTemplateService;

  @Mock
  TaskAssignmentRepository taskAssignmentRepository;
  @Mock
  TaskAssignmentHistoryService taskAssignmentHistoryService;


  @Test
  @DisplayName("그룹별 템플릿 조회 - active=true만 반환")
  void getAllTemplates_byGroup_filtersActive() {
    TaskTemplate active = TaskTemplate.builder().groupId(1L).category("CLEANING").randomEnabled(true).active(true).build();
    TaskTemplate inactive = TaskTemplate.builder().groupId(1L).category("TRASH").randomEnabled(false).active(false).build();
    when(taskTemplateRepository.findByGroupId(1L)).thenReturn(List.of(active, inactive));

    List<TaskTemplate> list = taskTemplateService.getAllTemplates(1L);

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getCategory()).isEqualTo("CLEANING");
    verify(taskTemplateRepository).findByGroupId(1L);
  }

  @Test
  @DisplayName("템플릿 생성 - 요일 1개면 addRepeatDay 1회 호출 + randomEnabled 반영")
  void createTemplate_oneDay_randomOn() {
    when(taskTemplateRepository.save(any(TaskTemplate.class)))
        .thenAnswer(inv -> {
          TaskTemplate t = inv.getArgument(0);
          ReflectionTestUtils.setField(t, "id", 100L);
          return t;
        });

    TaskTemplate saved = taskTemplateService.createTemplate(
        10L, "CLEANING", java.util.Collections.singletonList("SUNDAY"), true
    );

    assertThat(saved.getId()).isEqualTo(100L);
    assertThat(saved.getGroupId()).isEqualTo(10L);
    assertThat(saved.getCategory()).isEqualTo("CLEANING");
    assertThat(saved.isRandomEnabled()).isTrue();

    verify(repeatDayService, times(1))
        .addRepeatDay(org.mockito.ArgumentMatchers.eq(100L), any(RepeatDayRequest.class));
  }

  @Test
  @DisplayName("템플릿 생성 - 여러 요일이면 addRepeatDay N회 호출 + randomEnabled=false")
  void createTemplate_multiDays_randomOff() {
    when(taskTemplateRepository.save(any(TaskTemplate.class)))
        .thenAnswer(inv -> {
          TaskTemplate t = inv.getArgument(0);
          ReflectionTestUtils.setField(t, "id", 200L);
          return t;
        });

    var days = Arrays.asList("SUNDAY", "TUESDAY", "FRIDAY");
    TaskTemplate saved = taskTemplateService.createTemplate(10L, "CLEANING", days, false);

    assertThat(saved.getId()).isEqualTo(200L);
    assertThat(saved.isRandomEnabled()).isFalse();
    verify(repeatDayService, times(3))
        .addRepeatDay(org.mockito.ArgumentMatchers.eq(200L), any(RepeatDayRequest.class));
  }

  @Test
  @DisplayName("템플릿 수정 - 카테고리/랜덤/반복요일 세트 교체(옵션 필드만 반영)")
  void updateTemplate_ok_partialFields() {
    // given
    Long templateId = 123L;
    TaskTemplate found = TaskTemplate.builder().groupId(1L).category("OLD").randomEnabled(false).active(true).build();
    ReflectionTestUtils.setField(found, "id", templateId);

    when(taskTemplateRepository.findById(templateId)).thenReturn(Optional.of(found));
    when(taskTemplateRepository.save(found)).thenReturn(found);

    TaskTemplateUpdateRequest req = new TaskTemplateUpdateRequest();
    req.setCategory("NEW");
    req.setRandomEnabled(true);
    req.setRepeatDays(Arrays.asList("MONDAY", "WEDNESDAY"));

    // when
    TaskTemplate updated = taskTemplateService.updateTemplate(templateId, req);

    // then
    assertThat(updated.getCategory()).isEqualTo("NEW");
    assertThat(updated.isRandomEnabled()).isTrue();
    verify(repeatDayService).replaceRepeatDays(templateId, Arrays.asList("MONDAY", "WEDNESDAY"));
    verify(taskTemplateRepository).save(found);
  }

  @Test
  @DisplayName("템플릿 수정 - 대상 없음 -> CustomException(TEMPLATE_NOT_FOUND)")
  void updateTemplate_notFound() {
    Long templateId = 999L;
    when(taskTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

    TaskTemplateUpdateRequest req = new TaskTemplateUpdateRequest();
    req.setCategory("ANY");

    assertThatThrownBy(() -> taskTemplateService.updateTemplate(templateId, req))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND);

    verify(taskTemplateRepository).findById(templateId);
    verify(taskTemplateRepository, never()).save(any());
    verify(repeatDayService, never()).replaceRepeatDays(org.mockito.ArgumentMatchers.anyLong(), any());
  }

  @Test
  @DisplayName("템플릿 삭제 - 소프트 삭제(active=false)로 저장")
  void deleteTemplate_ok() {
    Long templateId = 11L;
    TaskTemplate found = TaskTemplate.builder()
        .groupId(10L).category("CLEANING").randomEnabled(true).active(true)
        .build();
    ReflectionTestUtils.setField(found, "id", templateId);

    when(taskTemplateRepository.findById(templateId)).thenReturn(Optional.of(found));

    taskTemplateService.deleteTemplate(templateId);

    verify(taskTemplateRepository).findById(templateId);
    verify(taskTemplateRepository).save(org.mockito.ArgumentMatchers.argThat(t ->
        t.getId().equals(templateId) && !t.isActive()
    ));
    verify(taskTemplateRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  @DisplayName("템플릿 삭제 - 없으면 CustomException(TEMPLATE_NOT_FOUND)")
  void deleteTemplate_notFound() {
    Long templateId = 77L;
    when(taskTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskTemplateService.deleteTemplate(templateId))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND);

    verify(taskTemplateRepository).findById(templateId);
    verify(taskTemplateRepository, never()).save(any());
    verify(taskTemplateRepository, never()).deleteById(any());
  }
}
