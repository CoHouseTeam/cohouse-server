package com.zero.cohousesever.task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.task.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.task.dto.assignment.UncompletedByMemberResponse;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import com.zero.cohousesever.task.repository.RepeatDayRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import com.zero.cohousesever.task.repository.TaskTemplateRepository;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceUncompletedTest {

  @Mock TaskAssignmentRepository assignmentRepo;
  @Mock TaskTemplateRepository templateRepo;
  @Mock RepeatDayRepository repeatRepo;

  @InjectMocks TaskAssignmentService service;

  // ===== helpers =====
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private TaskTemplate tpl(long tid, long gid, String cat) {
    TaskTemplate t = TaskTemplate.builder().groupId(gid).category(cat).build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }

  private TaskAssignment ta(long id, TaskTemplate t, long memberId, LocalDate date, AssignmentStatus st) {
    TaskAssignment a = TaskAssignment.builder()
        .template(t)
        .groupMemberId(memberId)
        .date(date)
        .status(st)
        .build();
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }

  private LocalDate[] thisWeekRange() {
    LocalDate today = LocalDate.now(KST);
    LocalDate sun = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    return new LocalDate[]{sun, sun.plusDays(6)};
  }

  @Test
  void getUncompletedThisWeek_allMembers_mapsRepeatType_andFiltersCompleted() {
    long gid = 1L;
    var range = thisWeekRange();
    LocalDate sun = range[0];
    LocalDate mon = sun.plusDays(1);
    LocalDate wed = sun.plusDays(3);

    // 템플릿: t1=weekly,
    var t1 = tpl(10L, gid, "청소");
    var t2 = tpl(20L, gid, "분리수거");

    var a1 = ta(101L, t1, 11L, mon, AssignmentStatus.PENDING);   // WEEKLY
    var a2 = ta(102L, t2, 22L, wed, AssignmentStatus.PENDING);   // NONE

    when(assignmentRepo.findByTemplate_GroupIdAndDateBetweenAndStatusNot(
        eq(gid), eq(range[0]), eq(range[1]), eq(AssignmentStatus.COMPLETED)))
        .thenReturn(List.of(a1, a2));

    when(repeatRepo.findTemplateIdsHavingRepeat(new HashSet<>(Arrays.asList(10L, 20L))))
        .thenReturn(Set.of(10L)); // 10L만 WEEKLY

    List<TaskAssignmentResponse> out = service.getUncompletedThisWeek(gid, null);

    assertEquals(2, out.size());
    // a1 WEEKLY
    TaskAssignmentResponse r1 = out.stream().filter(r -> r.getAssignmentId().equals(101L)).findFirst().orElseThrow();
    assertEquals("WEEKLY", r1.getRepeatType());
    assertEquals(11L, r1.getGroupMemberId());
    // a2 NONE
    TaskAssignmentResponse r2 = out.stream().filter(r -> r.getAssignmentId().equals(102L)).findFirst().orElseThrow();
    assertEquals("NONE", r2.getRepeatType());
    assertEquals(22L, r2.getGroupMemberId());

    verify(assignmentRepo).findByTemplate_GroupIdAndDateBetweenAndStatusNot(
        eq(gid), eq(range[0]), eq(range[1]), eq(AssignmentStatus.COMPLETED));
    verify(repeatRepo).findTemplateIdsHavingRepeat(new HashSet<>(Arrays.asList(10L, 20L)));
  }

  @Test
  void getUncompletedThisWeekByMember_groups_and_counts_desc() {
    long gid = 1L;
    var range = thisWeekRange();
    LocalDate sun = range[0];
    LocalDate mon = sun.plusDays(1);
    LocalDate tue = sun.plusDays(2);
    LocalDate fri = sun.plusDays(5);

    var t1 = tpl(10L, gid, "청소");
    var t2 = tpl(20L, gid, "분리수거");
    var t3 = tpl(30L, gid, "욕실청소");

    var a1 = ta(201L, t1, 11L, mon, AssignmentStatus.PENDING); // WEEKLY
    var a2 = ta(202L, t2, 22L, tue, AssignmentStatus.PENDING); // NONE
    var a3 = ta(203L, t3, 11L, fri, AssignmentStatus.PENDING); // NONE

    when(assignmentRepo.findByTemplate_GroupIdAndDateBetweenAndStatusNot(
        eq(gid), eq(range[0]), eq(range[1]), eq(AssignmentStatus.COMPLETED)))
        .thenReturn(List.of(a1, a2, a3));

    // t1만 반복 요일 보유
    when(repeatRepo.findTemplateIdsHavingRepeat(new HashSet<>(Arrays.asList(10L, 20L, 30L))))
        .thenReturn(Set.of(10L));

    List<UncompletedByMemberResponse> out = service.getUncompletedThisWeekByMember(gid);

    assertEquals(2, out.size());
    assertEquals(11L, out.get(0).getGroupMemberId());
    assertEquals(2, out.get(0).getCount());
    assertEquals(22L, out.get(1).getGroupMemberId());
    assertEquals(1, out.get(1).getCount());

    // 각 그룹의 repeatType 매핑 확인
    var firstAssignments = out.get(0).getAssignments();
    assertTrue(firstAssignments.stream().anyMatch(r -> r.getAssignmentId().equals(201L) && "WEEKLY".equals(r.getRepeatType())));
    assertTrue(firstAssignments.stream().anyMatch(r -> r.getAssignmentId().equals(203L) && "NONE".equals(r.getRepeatType())));
  }
}
