package com.zero.cohousesever.tasks.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.entity.enums.AssignmentStatus;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceTest {

  @Mock TaskAssignmentRepository assignmentRepo;
  @Mock TaskTemplateRepository templateRepo;
  @Mock RepeatDayRepository repeatRepo;

  @InjectMocks TaskAssignmentService service;

  // ===== helpers =====
  private TaskTemplate tpl(long tid, long gid, String cat) {
    TaskTemplate t = TaskTemplate.builder().groupId(gid).category(cat).build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }
  private RepeatDay rd(TaskTemplate t, DayOfWeek d) {
    return RepeatDay.builder().taskTemplate(t).dayOfWeek(d).build();
  }
  private TaskAssignment ta(long id, long tid, long gid, long memberId,
      String cat, LocalDate date, AssignmentStatus st) {
    TaskAssignment a = TaskAssignment.builder()
        .template(tpl(tid, gid, cat))
        .groupMemberId(memberId)
        .date(date)
        .status(st)
        .build();
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }
  private TaskAssignmentRequest req(String date, List<Long> candidates) {
    TaskAssignmentRequest r = new TaskAssignmentRequest();
    r.setGroupId(1L);
    r.setTemplateId(10L);
    r.setDate(date);
    // DTO가 List<Long> groupMemberId 사용 중
    r.setGroupMemberId(candidates);
    return r;
  }

  // ===== 생성 테스트 =====
  @Test
  void assign_success_twoDays_sameAssignee() {
    var t = tpl(10L, 1L, "청소");
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));
    // 서비스가 이번 주 중복 날짜 스킵용으로 전체를 읽음
    when(assignmentRepo.findByTemplate_Id(10L)).thenReturn(List.of());
    when(assignmentRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var request = req("2025-08-19", List.of(100L)); // 후보 1명 → 고정

    List<TaskAssignmentResponse> out = service.assignTaskManuallyOrRandomly(request);

    assertEquals(2, out.size());
    assertTrue(out.stream().allMatch(r -> r.getGroupMemberId().equals(100L)));
    verify(assignmentRepo).findByTemplate_Id(10L);
    verify(assignmentRepo).saveAll(anyList());
  }

  @Test
  void assign_duplicate_skipped_returnsEmpty() {
    var t = tpl(10L, 1L, "청소");
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));

    //이번 주(일→토) 기준. date=2025-08-19(Tue) → 주 시작 Sunday=2025-08-17
    LocalDate base   = LocalDate.parse("2025-08-19");
    LocalDate sunday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate tue    = sunday.plusDays(DayOfWeek.TUESDAY.getValue() % 7);   // 2025-08-19
    LocalDate thu    = sunday.plusDays(DayOfWeek.THURSDAY.getValue() % 7);  // 2025-08-21

    // 이번 주에 이미 동일 날짜 배정이 있다고 가정
    when(assignmentRepo.findByTemplate_Id(10L))
        .thenReturn(List.of(
            ta(901, 10, 1, 111, "청소", tue, AssignmentStatus.PENDING),
            ta(902, 10, 1, 222, "청소", thu, AssignmentStatus.PENDING)
        ));

    var out = service.assignTaskManuallyOrRandomly(req("2025-08-19", List.of(1L)));

    assertTrue(out.isEmpty());
    verify(assignmentRepo).findByTemplate_Id(10L);
    verify(assignmentRepo, never()).saveAll(anyList());
  }

  @Test
  void assign_missingCandidates_throws() {
    CustomException ex = assertThrows(CustomException.class,
        () -> service.assignTaskManuallyOrRandomly(req("2025-08-19", List.of())));
    assertEquals(ErrorCode.CANDIDATE_MEMBERS_REQUIRED, ex.getErrorCode());
  }

  // ===== 조회 테스트 (주 단위 + 멤버) =====
  @Test
  void getAssignments_weekly_member_filtersAndMapsRepeatType() {
    LocalDate from = LocalDate.of(2025, 8, 17); // Sun
    LocalDate to   = LocalDate.of(2025, 8, 23); // Sat

    //  서비스는 일→토
    LocalDate fromSun = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate toSat   = to.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

    var d1 = LocalDate.of(2025, 8, 18); // Mon
    var d2 = LocalDate.of(2025, 8, 19); // Tue

    when(assignmentRepo.findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(1L, 1L, fromSun, toSat))
        .thenReturn(List.of(
            ta(1, 10, 1, 1, "CLEAN", d1, AssignmentStatus.PENDING),
            ta(2, 20, 1, 1, "TRASH", d2, AssignmentStatus.COMPLETED)
        ));

    when(repeatRepo.existsByTaskTemplate_Id(10L)).thenReturn(true);
    when(repeatRepo.existsByTaskTemplate_Id(20L)).thenReturn(false);

    List<TaskAssignmentResponse> out = service.getAssignments(1L, from, to, 1L);

    assertEquals(2, out.size());
    assertEquals(1L, out.get(0).getAssignmentId());
    assertEquals("WEEKLY", out.get(0).getRepeatType());
    assertEquals(2L, out.get(1).getAssignmentId());
    assertEquals("NONE", out.get(1).getRepeatType());

    verify(assignmentRepo).findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(1L, 1L, fromSun, toSat);
    verify(repeatRepo).existsByTaskTemplate_Id(10L);
    verify(repeatRepo).existsByTaskTemplate_Id(20L);
  }
}
