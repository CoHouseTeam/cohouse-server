package com.zero.cohousesever.task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.TaskAssignmentHistory;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import com.zero.cohousesever.task.repository.RepeatDayRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentHistoryRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import java.time.LocalDate;
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

  // 히스토리 upsert - 신규 생성
  @Test
  void recordStatusChange_creates_whenNotExists() {
    LocalDate d = LocalDate.of(2025, 8, 29);
    TaskAssignment a = ta(1L, 10L, 1L, 111L, d);
    a.setStatus(AssignmentStatus.COMPLETED);

    when(historyRepo.findByAssignmentIdAndDate(1L, d)).thenReturn(Optional.empty());

    historyReal.recordStatusChange(a);

    verify(historyRepo).save(argThat(h ->
        h.getId() == null
            && h.getAssignmentId().equals(1L)
            && h.getDate().equals(d)
            && h.getGroupMemberId().equals(111L)
            && "청소".equals(h.getCategory())
            && h.getStatus() == AssignmentStatus.COMPLETED
    ));
  }

  // 히스토리 upsert - 업데이트
  @Test
  void recordStatusChange_updates_whenExists() {
    LocalDate d = LocalDate.of(2025, 8, 29);
    TaskAssignment a = ta(1L, 10L, 1L, 222L, d);
    a.setStatus(AssignmentStatus.SKIPPED);

    TaskAssignmentHistory existing = new TaskAssignmentHistory();
    ReflectionTestUtils.setField(existing, "id", 999L);
    existing.setAssignmentId(1L);
    existing.setDate(d);

    when(historyRepo.findByAssignmentIdAndDate(1L, d)).thenReturn(Optional.of(existing));

    historyReal.recordStatusChange(a);

    verify(historyRepo).save(argThat(h ->
        h.getId().equals(999L) // 같은 row 업데이트
            && h.getAssignmentId().equals(1L)
            && h.getDate().equals(d)
            && h.getGroupMemberId().equals(222L)
            && "청소".equals(h.getCategory())
            && h.getStatus() == AssignmentStatus.SKIPPED
    ));
  }
}
