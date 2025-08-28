package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskTemplateServiceTest {

  @Mock TaskTemplateRepository taskTemplateRepository;
  @Mock RepeatDayService repeatDayService;
  @InjectMocks TaskTemplateService taskTemplateService;

  @Test
  @DisplayName("그룹별 템플릿 조회")
  void getAllTemplates_byGroup() {
    // given
    TaskTemplate t1 = TaskTemplate.builder().groupId(1L).category("CLEANING").build();
    TaskTemplate t2 = TaskTemplate.builder().groupId(1L).category("TRASH").build();
    when(taskTemplateRepository.findByGroupId(1L)).thenReturn(List.of(t1, t2));

    // when
    List<TaskTemplate> list = taskTemplateService.getAllTemplates(1L);

    // then
    assertThat(list).hasSize(2);
    assertThat(list).extracting(TaskTemplate::getCategory)
        .containsExactlyInAnyOrder("CLEANING", "TRASH");
    verify(taskTemplateRepository).findByGroupId(1L);
  }

  @Test
  @DisplayName("템플릿 생성 - 요일 1개")
  void createTemplate_oneDay() {
    // given: save()가 넘긴 엔티티에 id 세팅해서 반환
    when(taskTemplateRepository.save(any(TaskTemplate.class)))
        .thenAnswer(inv -> {
          TaskTemplate t = inv.getArgument(0);
          ReflectionTestUtils.setField(t, "id", 100L);
          return t;
        });

    // when
    TaskTemplate saved = taskTemplateService.createTemplate(
        10L, "CLEANING", java.util.Collections.singletonList("SUNDAY")
    );

    // then
    assertThat(saved.getId()).isEqualTo(100L);
    assertThat(saved.getGroupId()).isEqualTo(10L);
    assertThat(saved.getCategory()).isEqualTo("CLEANING");

    // addRepeatDay가 1번 호출되었는지 확인 (반환값은 안 써서 stubbing 불필요)
    verify(repeatDayService, times(1))
        .addRepeatDay(eq(100L), any(com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest.class));
  }

  @Test
  @DisplayName("템플릿 생성 - 여러 요일")
  void createTemplate_multiDays() {
    when(taskTemplateRepository.save(any(TaskTemplate.class)))
        .thenAnswer(inv -> {
          TaskTemplate t = inv.getArgument(0);
          ReflectionTestUtils.setField(t, "id", 200L);
          return t;
        });

    var days = java.util.Arrays.asList("SUNDAY", "TUESDAY", "FRIDAY");

    TaskTemplate saved = taskTemplateService.createTemplate(10L, "CLEANING", days);

    assertThat(saved.getId()).isEqualTo(200L);
    verify(repeatDayService, times(3))
        .addRepeatDay(eq(200L), any(com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest.class));
  }


  @Test
  @DisplayName("템플릿 수정 - 존재하면 카테고리 변경")
  void updateTemplate_ok() {
    TaskTemplate found = TaskTemplate.builder().groupId(1L).category("OLD").build();
    ReflectionTestUtils.setField(found, "id", 123L);
    when(taskTemplateRepository.findById(123L)).thenReturn(Optional.of(found));
    when(taskTemplateRepository.save(found)).thenReturn(found);

    TaskTemplate updated = taskTemplateService.updateTemplate(123L, "NEW");

    assertThat(updated.getCategory()).isEqualTo("NEW");
    verify(taskTemplateRepository).save(found);
  }

//  @Test
//  @DisplayName("템플릿 수정 - 없으면 예외")
//  void updateTemplate_notFound() {
//    when(taskTemplateRepository.findById(999L)).thenReturn(Optional.empty());
//    assertThatThrownBy(() -> taskTemplateService.updateTemplate(999L, "ANY"))
//        .isInstanceOf(EntityNotFoundException.class);
//  }

  @Test
  @DisplayName("템플릿 삭제 - 존재 확인 후 삭제")
  void deleteTemplate_ok() {
    when(taskTemplateRepository.existsById(11L)).thenReturn(true);
    taskTemplateService.deleteTemplate(11L);
    verify(taskTemplateRepository).deleteById(11L);
  }

//  @Test
//  @DisplayName("템플릿 삭제 - 없으면 예외")
//  void deleteTemplate_notFound() {
//    when(taskTemplateRepository.existsById(77L)).thenReturn(false);
//    assertThatThrownBy(() -> taskTemplateService.deleteTemplate(77L))
//        .isInstanceOf(EntityNotFoundException.class);
//  }
}
