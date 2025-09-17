package com.zero.cohousesever.task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import com.zero.cohousesever.task.repository.RepeatDayRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentHistoryRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceUpdateStatusTest {

  @Mock TaskAssignmentRepository assignmentRepo;
  @Mock RepeatDayRepository repeatRepo;

  @Mock TaskAssignmentHistoryService historyServiceMock;

  @InjectMocks TaskAssignmentService service;

  @Mock TaskAssignmentHistoryRepository historyRepo;
  @InjectMocks TaskAssignmentHistoryService historyReal;

  // ===== helpers =====
  private TaskTemplate tpl(long tid, long gid) {
    TaskTemplate t = TaskTemplate.builder().groupId(gid).category("청소").build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }

  private TaskAssignment ta(long id, long tplId, long gid, long assignee, LocalDate date) {
    TaskAssignment a = TaskAssignment.builder()
        .template(tpl(tplId, gid)).groupMemberId(assignee).date(date).build();
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }

  // 히스토리 서비스 호출 + repeatType 반환
  @Test
  void updateStatus_callsHistoryService_andReturnsRepeatType() {
    TaskAssignment a = ta(1L, 10L, 1L, 111L, LocalDate.now());
    when(assignmentRepo.findById(1L)).thenReturn(Optional.of(a));
    when(assignmentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(repeatRepo.existsByTaskTemplate_Id(10L)).thenReturn(true);

    var res = service.updateAssignmentStatus(1L, AssignmentStatus.COMPLETED);

    assertEquals(AssignmentStatus.COMPLETED, res.getStatus());
    assertEquals("WEEKLY", res.getRepeatType());
    verify(historyServiceMock, times(1)).recordStatusChange(a);
  }

  //업서트 경로 검증: recordStatusChange 가 upsertHistory 를 정확히 호출하는지
  @Test
  void recordStatusChange_callsUpsert_withCorrectParams() {
    LocalDate d = LocalDate.of(2025, 8, 29);
    TaskAssignment a = ta(1L, 10L, 1L, 111L, d);
    a.setStatus(AssignmentStatus.COMPLETED);

    historyReal.recordStatusChange(a);

    verify(historyRepo, times(1)).upsertHistory(
        eq(1L),                 // assignmentId
        eq(111L),               // groupMemberId
        eq("청소"),             // category
        eq("COMPLETED"),        // status name()
        argThat(sqlDate -> sqlDate.toLocalDate().equals(d)) // date
    );
    verifyNoMoreInteractions(historyRepo);
  }

  // (선택) 여러 건 생성 시, 각 건마다 업서트 호출되는지 검증
  @Test
  void recordCreatedAssignments_callsUpsert_forEachAssignment() {
    LocalDate d1 = LocalDate.of(2025, 8, 30);
    LocalDate d2 = LocalDate.of(2025, 8, 31);

    TaskAssignment a1 = ta(1L, 10L, 1L, 111L, d1);
    a1.setStatus(AssignmentStatus.PENDING);
    TaskAssignment a2 = ta(2L, 10L, 1L, 222L, d2);
    a2.setStatus(AssignmentStatus.PENDING);

    historyReal.recordCreatedAssignments(List.of(a1, a2));

    verify(historyRepo, times(1)).upsertHistory(
        eq(1L), eq(111L), eq("청소"), eq("PENDING"),
        argThat(sqlDate -> sqlDate.toLocalDate().equals(d1))
    );
    verify(historyRepo, times(1)).upsertHistory(
        eq(2L), eq(222L), eq("청소"), eq("PENDING"),
        argThat(sqlDate -> sqlDate.toLocalDate().equals(d2))
    );
    verifyNoMoreInteractions(historyRepo);
  }
}
