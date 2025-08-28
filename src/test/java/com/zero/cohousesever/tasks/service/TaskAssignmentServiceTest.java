package com.zero.cohousesever.tasks.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
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
import java.util.Set;

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
  @Mock GroupMemberRepository groupMemberRepo;

  @InjectMocks TaskAssignmentService service;

  private TaskTemplate tpl(long tid, long gid, String cat) {
    TaskTemplate t = TaskTemplate.builder()
        .groupId(gid)
        .category(cat)
        .randomEnabled(false) // 기본값: 직전 담당자 유지
        .build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }

  private TaskTemplate tpl(long tid, long gid, String cat, boolean randomEnabled) {
    TaskTemplate t = TaskTemplate.builder()
        .groupId(gid)
        .category(cat)
        .randomEnabled(randomEnabled)
        .build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }

  private RepeatDay rd(TaskTemplate t, DayOfWeek d) {
    return RepeatDay.builder().taskTemplate(t).dayOfWeek(d).build();
  }

  private TaskAssignment ta(long id, TaskTemplate t, long memberId, LocalDate date, AssignmentStatus st) {
    TaskAssignment a = TaskAssignment.builder().template(t).groupMemberId(memberId).date(date).status(st).build();
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }

  private TaskAssignmentRequest req(String date, List<Long> candidates, Boolean randomEnabled, Long fixedAssigneeId) {
    TaskAssignmentRequest r = new TaskAssignmentRequest();
    r.setGroupId(1L);
    r.setTemplateId(10L);
    r.setDate(date);
    r.setGroupMemberId(candidates);
    r.setRandomEnabled(randomEnabled);
    // fixedAssigneeId가 있으면 그 사람으로 강제 배정
    ReflectionTestUtils.setField(r, "fixedAssigneeId", fixedAssigneeId);
    return r;
  }

  @Test
  void assign_success_twoDays_sameAssignee_thisWeek() {
    var t = tpl(10L, 1L, "청소", false); // 직전담당 유지(=랜덤X)
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));

    // date=2025-08-19(Tue) → 이번 주 일~토는 2025-08-17 ~ 2025-08-23
    LocalDate base   = LocalDate.parse("2025-08-19");
    LocalDate sun    = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate weekFrom = sun, weekTo = sun.plusDays(6);

    // 해당 주 템플릿 배정 없음 → 생성
    when(assignmentRepo.findByTemplate_IdAndDateBetween(10L, weekFrom, weekTo)).thenReturn(List.of());
    // 이번 주 전체 부하 계산용
    when(assignmentRepo.findByTemplate_GroupIdAndDateBetween(1L, weekFrom, weekTo)).thenReturn(List.of());
    // 직전 담당자 조회: 없음 → 우선순위 선택 로직으로
    when(assignmentRepo.findTopByTemplate_IdAndDateLessThanOrderByDateDesc(10L, weekFrom)).thenReturn(null);
    // 저장시 그대로 반환
    when(assignmentRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var request = req("2025-08-19", List.of(100L), null, null); // randomEnabled=null → 템플릿값 사용(false)
    List<TaskAssignmentResponse> out = service.assignTaskManuallyOrRandomly(request);

    assertEquals(2, out.size());
    assertTrue(out.stream().allMatch(r -> r.getGroupMemberId().equals(100L)));
    verify(assignmentRepo).findByTemplate_IdAndDateBetween(10L, weekFrom, weekTo);
    verify(assignmentRepo).findByTemplate_GroupIdAndDateBetween(1L, weekFrom, weekTo);
    verify(assignmentRepo).saveAll(anyList());
  }

  @Test
  void assign_duplicate_skipped_returnsEmpty() {
    var t = tpl(10L, 1L, "청소");
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.TUESDAY), rd(t, DayOfWeek.THURSDAY)));

    LocalDate base   = LocalDate.parse("2025-08-19");
    LocalDate sun    = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate tue    = sun.plusDays(DayOfWeek.TUESDAY.getValue() % 7);
    LocalDate thu    = sun.plusDays(DayOfWeek.THURSDAY.getValue() % 7);
    LocalDate weekFrom = sun, weekTo = sun.plusDays(6);

    when(assignmentRepo.findByTemplate_IdAndDateBetween(10L, weekFrom, weekTo))
        .thenReturn(List.of(
            ta(901, t, 111L, tue, AssignmentStatus.PENDING),
            ta(902, t, 222L, thu, AssignmentStatus.PENDING)
        ));
    when(assignmentRepo.findByTemplate_GroupIdAndDateBetween(1L, weekFrom, weekTo)).thenReturn(List.of());

    var out = service.assignTaskManuallyOrRandomly(req("2025-08-19", List.of(1L), null, null));

    assertTrue(out.isEmpty());
    verify(assignmentRepo).findByTemplate_IdAndDateBetween(10L, weekFrom, weekTo);
    verify(assignmentRepo, never()).saveAll(anyList());
  }

  @Test
  void assign_missingCandidates_throws_whenNoFixedAssignee() {
    CustomException ex = assertThrows(CustomException.class,
        () -> service.assignTaskManuallyOrRandomly(req("2025-08-19", List.of(), null, null)));
    assertEquals(ErrorCode.CANDIDATE_MEMBERS_REQUIRED, ex.getErrorCode());
  }

  @Test
  void assign_fixedAssignee_forces_member_after_membership_check() {
    var t = tpl(10L, 1L, "청소", true); // 랜덤 템플릿이지만 fixedAssignee가 우선
    when(templateRepo.findById(10L)).thenReturn(Optional.of(t));
    when(repeatRepo.findByTaskTemplate_Id(10L))
        .thenReturn(List.of(rd(t, DayOfWeek.MONDAY), rd(t, DayOfWeek.WEDNESDAY)));

    LocalDate base   = LocalDate.parse("2025-08-18"); // Mon
    LocalDate sun    = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate weekFrom = sun, weekTo = sun.plusDays(6);

    when(assignmentRepo.findByTemplate_IdAndDateBetween(10L, weekFrom, weekTo)).thenReturn(List.of());
    // fixedAssigneeId 검증: 그룹 소속이어야 함
    when(groupMemberRepo.existsByGroupIdAndMemberId(1L, 777L)).thenReturn(true);
    when(assignmentRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var out = service.assignTaskManuallyOrRandomly(req("2025-08-18", null, null, 777L));

    assertEquals(2, out.size());
    assertTrue(out.stream().allMatch(r -> r.getGroupMemberId().equals(777L)));
    verify(groupMemberRepo).existsByGroupIdAndMemberId(1L, 777L);
  }

  @Test
  void getAssignments_weekly_member_filtersAndMapsRepeatType() {
    LocalDate from = LocalDate.of(2025, 8, 17); // Sun
    LocalDate to   = LocalDate.of(2025, 8, 23); // Sat

    LocalDate fromSun = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate toSat   = to.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

    var t1 = tpl(10L, 1L, "CLEAN");
    var t2 = tpl(20L, 1L, "TRASH");
    var d1 = LocalDate.of(2025, 8, 18); // Mon
    var d2 = LocalDate.of(2025, 8, 19); // Tue

    when(assignmentRepo.findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(1L, 1L, fromSun, toSat))
        .thenReturn(List.of(
            ta(1, t1, 1L, d1, AssignmentStatus.PENDING),
            ta(2, t2, 1L, d2, AssignmentStatus.COMPLETED)
        ));

    when(repeatRepo.findTemplateIdsHavingRepeat(Set.of(10L, 20L)))
        .thenReturn(Set.of(10L)); // 10만 WEEKLY

    List<TaskAssignmentResponse> out = service.getAssignments(1L, from, to, 1L);

    assertEquals(2, out.size());
    assertEquals(1L, out.get(0).getAssignmentId());
    assertEquals("WEEKLY", out.get(0).getRepeatType());
    assertEquals(2L, out.get(1).getAssignmentId());
    assertEquals("NONE", out.get(1).getRepeatType());

    verify(assignmentRepo).findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(1L, 1L, fromSun, toSat);
    verify(repeatRepo).findTemplateIdsHavingRepeat(Set.of(10L, 20L));
  }

  @Test
  void updateStatus_complete_returnsRepeatType() {
    var t = tpl(10L,1L,"청소");
    var a = ta(1, t, 11L, LocalDate.of(2025,8,19), AssignmentStatus.PENDING);

    when(assignmentRepo.findById(1L)).thenReturn(Optional.of(a));
    when(assignmentRepo.save(any(TaskAssignment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(repeatRepo.existsByTaskTemplate_Id(10L)).thenReturn(true);

    var res = service.updateAssignmentStatus(1L, AssignmentStatus.COMPLETED);

    assertEquals(AssignmentStatus.COMPLETED, res.getStatus());
    assertEquals("WEEKLY", res.getRepeatType());
  }
}
