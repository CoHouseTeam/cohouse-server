package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceTest {

  @Mock TaskAssignmentRepository assignmentRepo;
  @Mock TaskTemplateRepository templateRepo;
  @Mock RepeatDayRepository repeatRepo;

  @InjectMocks TaskAssignmentService service;

  private TaskTemplate template() {
    return TaskTemplate.builder().groupId(1L).category("청소").build();
  }

  private RepeatDay rd(TaskTemplate t, DayOfWeek d) {
    return RepeatDay.builder().taskTemplate(t).dayOfWeek(d).build();
  }

  private TaskAssignmentRequest req(String date, List<Long> candidates) {
    var r = new TaskAssignmentRequest();
    r.setGroupId(1L);
    r.setTemplateId(10L);
    r.setDate(date);               // null이면 서비스가 '다음 주' 기준
    r.setCandidateMemberIds(candidates);
    return r;
  }

  @Test
  void success_withExplicitDate_usesNextWeek_andSameAssignee() {
    // given
    var t = template();
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));
    when(assignmentRepo.existsByTemplate_IdAndDate(eq(10L), any(LocalDate.class))).thenReturn(false);
    when(assignmentRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var request = req("2025-08-19", List.of(100L)); // 랜덤 고정용: 후보 1명

    // when
    List<TaskAssignmentResponse> res =
        service.assignTaskManuallyOrRandomly(request, request.getCandidateMemberIds());

    // then
    assertEquals(2, res.size());
    assertTrue(res.stream().allMatch(r -> r.getGroupMemberId().equals(100L)));

    LocalDate base = LocalDate.parse("2025-08-19").plusWeeks(1);
    LocalDate sunday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    var expected = List.of(
        sunday.plusDays(DayOfWeek.TUESDAY.getValue() % 7).toString(),
        sunday.plusDays(DayOfWeek.THURSDAY.getValue() % 7).toString()
    );
    assertTrue(res.stream().allMatch(r -> expected.contains(r.getDate())));
  }

  @Test
  void success_whenDateIsNull_usesNextWeekFromToday() {
    var t = template();
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));
    when(assignmentRepo.existsByTemplate_IdAndDate(eq(10L), any(LocalDate.class))).thenReturn(false);
    when(assignmentRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var request = req(null, List.of(777L));

    List<TaskAssignmentResponse> res =
        service.assignTaskManuallyOrRandomly(request, request.getCandidateMemberIds());

    assertEquals(2, res.size());
    assertTrue(res.stream().allMatch(r -> r.getGroupMemberId().equals(777L)));

    LocalDate base = LocalDate.now(ZoneId.of("Asia/Seoul")).plusWeeks(1);
    LocalDate sunday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    var expected = List.of(
        sunday.plusDays(DayOfWeek.TUESDAY.getValue() % 7).toString(),
        sunday.plusDays(DayOfWeek.THURSDAY.getValue() % 7).toString()
    );
    assertTrue(res.stream().allMatch(r -> expected.contains(r.getDate())));
  }

  @Test
  void duplicate_allSkipped_returnsEmpty() {
    var t = template();
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));
    when(assignmentRepo.existsByTemplate_IdAndDate(eq(10L), any(LocalDate.class))).thenReturn(true);

    var request = req("2025-08-19", List.of(1L));

    List<TaskAssignmentResponse> res =
        service.assignTaskManuallyOrRandomly(request, request.getCandidateMemberIds());

    assertTrue(res.isEmpty());
    verify(assignmentRepo, never()).saveAll(anyList());
  }

  @Test
  void missingCandidates_throws() {
    var request = req("2025-08-19", List.of());
    CustomException ex = assertThrows(CustomException.class,
        () -> service.assignTaskManuallyOrRandomly(request, request.getCandidateMemberIds()));
    assertEquals(ErrorCode.CANDIDATE_MEMBERS_REQUIRED, ex.getErrorCode());
  }
}
