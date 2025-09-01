package com.zero.cohousesever.tasks.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AssignmentOverrideServiceTest {

  @Mock AssignmentOverrideRepository overrideRepo;
  @Mock TaskAssignmentRepository assignmentRepo;
  @Mock GroupMemberRepository groupMemberRepo;

  @InjectMocks AssignmentOverrideService service;
  @Mock AssignmentOverrideHistoryService overrideHistoryService;

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
    // 외부 게시판 알림 비활성(NO-OP)
    ReflectionTestUtils.setField(service, "boardApiUrl", "");
  }

  // ===== 생성: 대상 미지정 → 멤버별 요청 row 생성 =====
  @Test
  void create_noTarget_createsRequests_perActiveMembers_exceptRequester() {
    long assignmentId = 1L, gid = 10L, requester = 101L;
    TaskAssignment a = ta(assignmentId, 100L, gid, requester, LocalDate.now());

    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));

    // ACTIVE 멤버 4명 (요청자 포함)
    when(groupMemberRepo.findMemberIdsByGroupIdAndStatus(gid, GroupMemberStatus.ACTIVE))
        .thenReturn(new java.util.ArrayList<>(List.of(101L, 201L, 202L, 203L)));

    // ★ 소속 검증 스텁 추가: 요청자를 제외한 대상자 모두 true
    when(groupMemberRepo.existsByGroupIdAndMemberId(eq(gid), anyLong())).thenAnswer(inv -> {
      Long memberId = inv.getArgument(1, Long.class);
      return Set.of(101L, 201L, 202L, 203L).contains(memberId);
    });

    // 중복 REQUESTED 없음
    when(overrideRepo.existsByAssignment_IdAndTargetIdAndStatus(eq(assignmentId), anyLong(), eq(OverrideStatus.REQUESTED)))
        .thenReturn(false);

    // save 시 id 채워서 반환
    when(overrideRepo.save(any(AssignmentOverride.class))).thenAnswer(inv -> {
      AssignmentOverride saved = inv.getArgument(0);
      org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", System.nanoTime());
      return saved;
    });

    AssignmentOverrideRequest req = new AssignmentOverrideRequest();
    req.setRequesterId(requester); // 대상 미지정

    List<AssignmentOverrideResponse> out = service.createOverrideRequests(assignmentId, req);

    // 요청자 제외 3명에게 생성
    assertEquals(3, out.size());
    assertTrue(out.stream().allMatch(r -> Set.of(201L, 202L, 203L).contains(r.getTargetId())));
    verify(overrideRepo, atLeast(3)).save(any(AssignmentOverride.class));
  }

  // ===== 생성: 지정 대상이 그룹 소속 아님 → 예외 =====
  @Test
  void create_designated_notSameGroup_throws() {
    long assignmentId = 1L, gid = 10L, requester = 101L, target = 202L;
    TaskAssignment a = ta(assignmentId, 100L, gid, requester, LocalDate.now());

    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(groupMemberRepo.existsByGroupIdAndMemberId(gid, target)).thenReturn(false);

    AssignmentOverrideRequest req = new AssignmentOverrideRequest();
    req.setRequesterId(requester);
    req.setTargetId(target);

    CustomException ex = assertThrows(CustomException.class,
        () -> service.createOverrideRequests(assignmentId, req));
    assertEquals(ErrorCode.OVERRIDE_NOT_SAME_GROUP, ex.getErrorCode());
  }

  // ===== 응답: 지정요청 ACCEPT → 담당자 교체 + 나머지 일괄 REJECT =====
  @Test
  void accept_designated_changesAssignee_and_bulkRejectsOthers() {
    long assignmentId = 1L, gid = 10L, current = 101L, actor = 202L;
    TaskAssignment a = ta(assignmentId, 100L, gid, current, LocalDate.now());
    AssignmentOverride r = ov(55L, a, current, actor, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(groupMemberRepo.existsByGroupIdAndMemberId(gid, actor)).thenReturn(true);
    when(overrideRepo.bulkUpdateStatusByAssignmentId(assignmentId, OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor))
        .thenReturn(2);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(actor);
    req.setStatus(OverrideStatus.ACCEPTED);

    AssignmentOverrideResponse res = service.respondToOverrideRequest(55L, req);

    assertEquals(actor, a.getGroupMemberId());
    assertEquals(OverrideStatus.ACCEPTED, res.getStatus());
    verify(overrideRepo).bulkUpdateStatusByAssignmentId(assignmentId, OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor);
  }

  // ===== 응답: 스왑 ACCEPT → 두 배정 담당자 맞교환 + 양쪽 일괄 REJECT =====
  @Test
  void accept_swap_exchangesBoth_and_bulkRejectsBothAssignments() {
    long gid = 10L;
    TaskAssignment first  = ta(1L, 100L, gid, 101L, LocalDate.now());
    TaskAssignment second = ta(2L, 200L, gid, 202L, LocalDate.now());
    AssignmentOverride r = ov(77L, first, 101L, 202L, OverrideStatus.REQUESTED);
    ReflectionTestUtils.setField(r, "swapAssignmentId", 2L);

    when(overrideRepo.findById(77L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(first));
    when(assignmentRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(second));
    when(groupMemberRepo.existsByGroupIdAndMemberId(gid, 202L)).thenReturn(true);
    when(overrideRepo.bulkUpdateStatusByAssignmentId(anyLong(), eq(OverrideStatus.REQUESTED), eq(OverrideStatus.REJECTED), eq(202L)))
        .thenReturn(2);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(202L);
    req.setStatus(OverrideStatus.ACCEPTED);

    AssignmentOverrideResponse res = service.respondToOverrideRequest(77L, req);

    assertEquals(OverrideStatus.ACCEPTED, res.getStatus());
    assertEquals(202L, first.getGroupMemberId());
    assertEquals(101L, second.getGroupMemberId());
    verify(overrideRepo).bulkUpdateStatusByAssignmentId(1L, OverrideStatus.REQUESTED, OverrideStatus.REJECTED, 202L);
    verify(overrideRepo).bulkUpdateStatusByAssignmentId(2L, OverrideStatus.REQUESTED, OverrideStatus.REJECTED, 202L);
  }

  // ===== 응답: 지정요청 REJECT - 대상자 아님 → 금지 =====
  @Test
  void reject_designated_wrongActor_forbidden() {
    long assignmentId = 1L, gid = 10L, current = 101L, target = 202L, actor = 303L;
    TaskAssignment a = ta(assignmentId, 100L, gid, current, LocalDate.now());
    AssignmentOverride r = ov(55L, a, current, target, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(groupMemberRepo.existsByGroupIdAndMemberId(gid, actor)).thenReturn(true);

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(actor);
    req.setStatus(OverrideStatus.REJECTED);

    CustomException ex = assertThrows(CustomException.class,
        () -> service.respondToOverrideRequest(55L, req));
    assertEquals(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET, ex.getErrorCode());
  }

  // ===== 응답: 지정요청 REJECT - 대상자 본인 → 성공, 남은 REQUESTED 없으면 '모두 거절' 상태 =====
  @Test
  void reject_designated_byTarget_success_and_whenNoneLeft_allRejectedCaseReachable() {
    long assignmentId = 1L, gid = 10L, current = 101L, target = 202L;
    TaskAssignment a = ta(assignmentId, 100L, gid, current, LocalDate.now());
    AssignmentOverride r = ov(55L, a, current, target, OverrideStatus.REQUESTED);

    when(overrideRepo.findById(55L)).thenReturn(Optional.of(r));
    when(assignmentRepo.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(a));
    when(groupMemberRepo.existsByGroupIdAndMemberId(gid, target)).thenReturn(true);
    // 남은 REQUESTED 없음  모두 거절 케이스
    when(overrideRepo.findAllByAssignment_IdAndStatus(assignmentId, OverrideStatus.REQUESTED))
        .thenReturn(List.of());

    AssignmentOverrideStatusUpdateRequest req = new AssignmentOverrideStatusUpdateRequest();
    req.setGroupMemberId(target);
    req.setStatus(OverrideStatus.REJECTED);

    AssignmentOverrideResponse res = service.respondToOverrideRequest(55L, req);

    assertEquals(OverrideStatus.REJECTED, res.getStatus());
    assertEquals(target, res.getModifierId());
    // 남은 요청 조회 수행됨(모두 거절 판단 지점 도달)
    verify(overrideRepo).findAllByAssignment_IdAndStatus(assignmentId, OverrideStatus.REQUESTED);
  }
}
