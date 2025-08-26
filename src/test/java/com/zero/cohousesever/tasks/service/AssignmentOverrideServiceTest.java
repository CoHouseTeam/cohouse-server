package com.zero.cohousesever.tasks.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideRequest;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideStatusUpdateRequest;
import com.zero.cohousesever.tasks.entity.AssignmentOverride;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import com.zero.cohousesever.tasks.repository.AssignmentOverrideRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AssignmentOverrideServiceTest {

  @Mock AssignmentOverrideRepository overrideRepo;
  @Mock TaskAssignmentRepository assignmentRepo;

  @InjectMocks AssignmentOverrideService service;

  // ===== helpers =====
  private TaskTemplate tpl(long tid, long gid) {
    TaskTemplate t = TaskTemplate.builder().groupId(gid).build();
    ReflectionTestUtils.setField(t, "id", tid);
    return t;
  }

  private TaskAssignment ta(long id, long tplId, long gid, long assignee, LocalDate date) {
    TaskAssignment a = TaskAssignment.builder()
        .template(tpl(tplId, gid))
        .groupMemberId(assignee)
        .date(date)
        .build();
    ReflectionTestUtils.setField(a, "id", id);
    return a;
  }

  private AssignmentOverride ov(long id, TaskAssignment a, long requester, long target, OverrideStatus st) {
    AssignmentOverride r = AssignmentOverride.builder()
        .assignment(a)
        .requesterId(requester)
        .targetId(target)
        .status(st)
        .build();
    ReflectionTestUtils.setField(r, "id", id);
    return r;
  }

  @BeforeEach
  void init() {
    // 알림은 NO-OP 경로(외부 호출 안 함)
    ReflectionTestUtils.setField(service, "boardApiUrl", "");
  }

  // ========== 생성 ==========
  @Test
  void create_broadcast_success() {
    long assignmentId = 1L;
    long gid = 10L;
    long requester = 101L;

    TaskAssignment a = ta(assignmentId, 100L, gid, requester, LocalDate.now());
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(overrideRepo.existsByAssignment_IdAndTargetIdAndStatus(eq(assignmentId), eq(0L), eq(OverrideStatus.REQUESTED)))
        .thenReturn(false);
    when(overrideRepo.save(any(AssignmentOverride.class))).thenAnswer(inv -> {
      AssignmentOverride saved = inv.getArgument(0);
      ReflectionTestUtils.setField(saved, "id", 999L);
      return saved;
    });

    AssignmentOverrideRequest req = new AssignmentOverrideRequest();
    req.setRequesterId(requester); // 현재 담당자 == 요청자

    List<AssignmentOverrideResponse> out = service.createOverrideRequests(assignmentId, req);

    assertEquals(1, out.size());
    assertEquals(0L, out.get(0).getTargetId()); // 브로드캐스트
    verify(overrideRepo).save(any(AssignmentOverride.class));
  }

  @Test
  void create_designated_differentGroup_throws() {
    long assignmentId = 1L;
    long gid = 10L;
    long requester = 101L;
    long target = 202L;

    TaskAssignment a = ta(assignmentId, 100L, gid, requester, LocalDate.now());
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    // target이 같은 그룹 아님
    when(assignmentRepo.existsByTemplate_GroupIdAndGroupMemberId(gid, target)).thenReturn(false);

    AssignmentOverrideRequest req = new AssignmentOverrideRequest();
    req.setRequesterId(requester);
    req.setTargetId(target);

    CustomException ex = assertThrows(CustomException.class,
        () -> service.createOverrideRequests(assignmentId, req));
    assertEquals(ErrorCode.OVERRIDE_NOT_SAME_GROUP, ex.getErrorCode());
  }

  // ========== 응답: 브로드캐스트 ACCEPT ==========
  @Test
  void accept_broadcast_changesAssignee_and_bulkRejectsOthers() {
    long assignmentId = 1L;
    long gid = 10L;
    long currentAssignee = 101L;
    long actor = 202L;

    TaskAssignment a = ta(assignmentId, 100L, gid, currentAssignee, LocalDate.now());
    AssignmentOverride r = ov(55L, a, currentAssignee, 0L, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(assignmentRepo.existsByTemplate_GroupIdAndGroupMemberId(gid, actor)).thenReturn(true);
    when(overrideRepo.bulkUpdateStatusByAssignmentId(eq(assignmentId), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(actor)))
        .thenReturn(3);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(actor);
    req.setStatus(OverrideStatus.ACCEPTED);

    AssignmentOverrideResponse res = service.respondToOverrideRequest(55L, req);

    assertEquals(actor, a.getGroupMemberId()); // 담당자 변경
    assertEquals(OverrideStatus.ACCEPTED, res.getStatus());
    assertEquals(actor, res.getTargetId());    // 수락자 기록
    verify(overrideRepo, times(1)).bulkUpdateStatusByAssignmentId(eq(assignmentId),
        eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(actor));
  }

  // ========== 응답: 스왑 ACCEPT ==========
  @Test
  void accept_swap_exchangesAssignees_and_bulkRejectsBoth() {
    long gid = 10L;
    TaskAssignment first  = ta(1L, 100L, gid, 101L, LocalDate.now());
    TaskAssignment second = ta(2L, 200L, gid, 202L, LocalDate.now());

    AssignmentOverride r = ov(77L, first, 101L, 202L, OverrideStatus.REQUESTED);
    ReflectionTestUtils.setField(r, "swapAssignmentId", 2L);

    when(overrideRepo.findById(77L)).thenReturn(Optional.of(r));
    // 오름차순 잠금 순서로 두 번 조회
    when(assignmentRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(first));
    when(assignmentRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(second));
    when(assignmentRepo.existsByTemplate_GroupIdAndGroupMemberId(gid, 202L)).thenReturn(true);
    when(overrideRepo.bulkUpdateStatusByAssignmentId(anyLong(), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L)))
        .thenReturn(2);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(202L);
    req.setStatus(OverrideStatus.ACCEPTED);

    AssignmentOverrideResponse res = service.respondToOverrideRequest(77L, req);

    assertEquals(OverrideStatus.ACCEPTED, res.getStatus());
    assertEquals(202L, first.getGroupMemberId());
    assertEquals(101L, second.getGroupMemberId());
    verify(overrideRepo).bulkUpdateStatusByAssignmentId(eq(1L), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L));
    verify(overrideRepo).bulkUpdateStatusByAssignmentId(eq(2L), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L));
  }

  // ========== 응답: 브로드캐스트 REJECT 금지 ==========
  @Test
  void reject_broadcast_forbidden() {
    long assignmentId = 1L;
    long gid = 10L;
    long currentAssignee = 101L;
    long actor = 202L;

    TaskAssignment a = ta(assignmentId, 100L, gid, currentAssignee, LocalDate.now());
    AssignmentOverride r = ov(55L, a, currentAssignee, 0L, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(assignmentRepo.existsByTemplate_GroupIdAndGroupMemberId(gid, actor)).thenReturn(true);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(actor);
    req.setStatus(OverrideStatus.REJECTED);

    CustomException ex = assertThrows(CustomException.class,
        () -> service.respondToOverrideRequest(55L, req));
    assertEquals(ErrorCode.OVERRIDE_BROADCAST_REJECT_FORBIDDEN, ex.getErrorCode());
  }

  // ========== 응답: 지정요청 REJECT 권한 체크 ==========
  @Test
  void reject_designated_wrongActor_forbidden() {
    long assignmentId = 1L;
    long gid = 10L;
    long currentAssignee = 101L;
    long target = 202L;
    long actor = 303L; // 대상자 아님

    TaskAssignment a = ta(assignmentId, 100L, gid, currentAssignee, LocalDate.now());
    AssignmentOverride r = ov(55L, a, currentAssignee, target, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(assignmentRepo.existsByTemplate_GroupIdAndGroupMemberId(gid, actor)).thenReturn(true);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(actor);
    req.setStatus(OverrideStatus.REJECTED);

    CustomException ex = assertThrows(CustomException.class,
        () -> service.respondToOverrideRequest(55L, req));
    assertEquals(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET, ex.getErrorCode());
  }

  @Test
  void reject_designated_byTarget_success() {
    long assignmentId = 1L;
    long gid = 10L;
    long currentAssignee = 101L;
    long target = 202L;

    TaskAssignment a = ta(assignmentId, 100L, gid, currentAssignee, LocalDate.now());
    AssignmentOverride r = ov(55L, a, currentAssignee, target, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(assignmentRepo.existsByTemplate_GroupIdAndGroupMemberId(gid, target)).thenReturn(true);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(target);
    req.setStatus(OverrideStatus.REJECTED);

    AssignmentOverrideResponse res = service.respondToOverrideRequest(55L, req);

    assertEquals(OverrideStatus.REJECTED, res.getStatus());
    assertEquals(target, res.getModifierId());
  }
}
