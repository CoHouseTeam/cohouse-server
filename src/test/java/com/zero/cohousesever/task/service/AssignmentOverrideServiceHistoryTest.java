package com.zero.cohousesever.task.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.post.service.PostService;
import com.zero.cohousesever.task.dto.override.AssignmentOverrideRequest;
import com.zero.cohousesever.task.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.task.dto.override.AssignmentOverrideStatusUpdateRequest;
import com.zero.cohousesever.task.entity.AssignmentOverride;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.entity.enums.OverrideStatus;
import com.zero.cohousesever.task.repository.AssignmentOverrideRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AssignmentOverrideServiceHistoryTest {

  @Mock AssignmentOverrideRepository overrideRepo;
  @Mock TaskAssignmentRepository assignmentRepo;
  @Mock GroupMemberRepository groupMemberRepo;
  @Mock AssignmentOverrideHistoryService historyService;
  @Mock PostService postService;
  @InjectMocks AssignmentOverrideService service;

  private TaskTemplate tpl(long tid, long gid) {
    TaskTemplate t = TaskTemplate.builder().groupId(gid).build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }
  private TaskAssignment ta(long id, long tplId, long gid, long assignee, LocalDate date) {
    TaskAssignment a = TaskAssignment.builder()
        .template(tpl(tplId, gid)).groupMemberId(assignee).date(date).build();
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }
  private AssignmentOverride ov(long id, TaskAssignment a, long requester, long target) {
    AssignmentOverride r = AssignmentOverride.builder()
        .assignment(a).requesterId(requester).targetId(target).status(OverrideStatus.REQUESTED).build();
    ReflectionTestUtils.setField(r, "id", id);
    return r;
  }

  @Test
  void designated_accept_recordsHistory_andBulkRejectsOthers() {
    long gid = 10L;
    TaskAssignment a = ta(1L, 100L, gid, 101L, LocalDate.now());
    AssignmentOverride r = ov(55L, a, 101L, 202L);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(a));
    when(groupMemberRepo.existsByGroupIdAndMemberId(gid, 202L)).thenReturn(true);
    when(overrideRepo.bulkUpdateStatusByAssignmentId(eq(1L), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L)))
        .thenReturn(2);

    AssignmentOverrideStatusUpdateRequest accept = new AssignmentOverrideStatusUpdateRequest();
    accept.setActorMemberId(202L);
    accept.setStatus(OverrideStatus.ACCEPTED);

    var res = service.respondToOverrideRequest(55L, accept);

    assertEquals(OverrideStatus.ACCEPTED, res.getStatus());
    assertEquals(202L, res.getModifierId());
    assertEquals(202L, a.getGroupMemberId());

    // 히스토리 기록
    verify(historyService, times(1)).record(eq(r), eq(202L), eq(202L), eq(0L));
    // 다른 요청 일괄 거절 호출
    verify(overrideRepo, times(1)).bulkUpdateStatusByAssignmentId(eq(1L),
        eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L));
  }

  @Test
  void broadcast_flow_accept_recordsHistory() {
    long assignmentId = 1L, gid = 10L, requester = 101L;
    TaskAssignment a = ta(assignmentId, 100L, gid, requester, LocalDate.now());

    // 1) 전체 요청 생성 준비
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(groupMemberRepo.findMemberIdsByGroupIdAndStatus(gid, GroupMemberStatus.ACTIVE))
        .thenReturn(new ArrayList<>(List.of(101L, 202L, 203L))); // 요청자 제외
    when(groupMemberRepo.existsByGroupIdAndMemberId(eq(gid), anyLong())).thenReturn(true);
    when(overrideRepo.existsByAssignment_IdAndTargetIdAndStatus(
        eq(assignmentId), anyLong(), eq(OverrideStatus.REQUESTED))
    ).thenReturn(false);

    // save될 때 부여된 엔티티를 findById에서 다시 돌려줄 수 있도록 보관
    Map<Long, AssignmentOverride> savedById = new HashMap<>();
    when(overrideRepo.save(any(AssignmentOverride.class))).thenAnswer(inv -> {
      AssignmentOverride e = inv.getArgument(0);
      long id = 1000L + e.getTargetId(); // 타겟별로 결정적 ID 부여(테스트 편의용)
      ReflectionTestUtils.setField(e, "id", id);
      savedById.put(id, e);
      return e;
    });

    // 2) 전체 요청 생성
    AssignmentOverrideRequest createReq = new AssignmentOverrideRequest();
    createReq.setRequesterId(requester);
    var created = service.createOverrideRequests(assignmentId, createReq);

    assertEquals(2, created.size()); // 202, 203 두 명에게 요청 생성

    long rowIdFor202 = created.stream()
        .filter(r -> Objects.equals(r.getTargetId(), 202L))
        .map(AssignmentOverrideResponse::getRequestId)
        .findFirst()
        .orElseThrow();

    when(overrideRepo.findById(rowIdFor202)).thenReturn(Optional.of(savedById.get(rowIdFor202)));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(overrideRepo.bulkUpdateStatusByAssignmentId(
        eq(assignmentId), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L))
    ).thenReturn(1);

    // 3) 202가 수락
    AssignmentOverrideStatusUpdateRequest accept = new AssignmentOverrideStatusUpdateRequest();
    accept.setActorMemberId(202L);
    accept.setStatus(OverrideStatus.ACCEPTED);

    var res = service.respondToOverrideRequest(rowIdFor202, accept);

    assertEquals(OverrideStatus.ACCEPTED, res.getStatus());
    assertEquals(202L, a.getGroupMemberId()); // 담당자 변경 확인
    verify(historyService).record(any(AssignmentOverride.class), eq(202L), eq(202L), eq(0L));
  }

}
