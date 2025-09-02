package com.zero.cohousesever.task.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.task.dto.override.AssignmentOverrideRequest;
import com.zero.cohousesever.task.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.task.dto.override.AssignmentOverrideStatusUpdateRequest;
import com.zero.cohousesever.task.entity.AssignmentOverride;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.enums.OverrideStatus;
import com.zero.cohousesever.task.repository.AssignmentOverrideRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AssignmentOverrideService {

  private final AssignmentOverrideRepository overrideRepository;
  private final TaskAssignmentRepository assignmentRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final AssignmentOverrideHistoryService overrideHistoryService;


  @Value("${board.api.url:}")
  private String boardApiUrl;

  // ========== 생성 (브로드캐스트 → 멤버별 요청 row 생성) ==========
  @Transactional
  public List<AssignmentOverrideResponse> createOverrideRequests(Long assignmentId, AssignmentOverrideRequest req) {
    if (req.getRequesterId() == null) throw new CustomException(ErrorCode.REQUESTER_ID_REQUIRED);

    TaskAssignment a = assignmentRepository.findByIdForUpdate(assignmentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    assertTodayOrFuture(a.getDate());
    assertCurrentAssignee(a, req.getRequesterId());

    // ----- 서로 변경(swap) 우선 처리 -----
    if (req.getSwapAssignmentId() != null) {
      TaskAssignment other = assignmentRepository.findByIdForUpdate(req.getSwapAssignmentId())
          .orElseThrow(() -> new CustomException(ErrorCode.OVERRIDE_SWAP_TARGET_NOT_FOUND));

      assertTodayOrFuture(other.getDate());
      assertSameGroup(a, other);

      Long targetId = other.getGroupMemberId();
      assertInSameGroup(a, targetId);

      // 이미 대상자에게 대기중이면 중복 생성 안 함
      if (overrideRepository.existsByAssignment_IdAndTargetIdAndStatus(a.getId(), targetId, OverrideStatus.REQUESTED)) {
        notifyBoard(a, req.getRequesterId(), Set.of(targetId), true);
        return List.of();
      }

      AssignmentOverride saved = overrideRepository.save(
          AssignmentOverride.builder()
              .assignment(a)
              .requesterId(req.getRequesterId())
              .targetId(targetId)
              .swapAssignmentId(other.getId())
              .status(OverrideStatus.REQUESTED)
              .build()
      );
      notifyBoard(a, req.getRequesterId(), Set.of(targetId), true);
      return List.of(AssignmentOverrideResponse.from(saved));
    }

    // ----- 지정/다중/전체(= 멤버별 생성) -----
    Set<Long> targets = collectTargets(req);
    final Long groupId = a.getTemplate().getGroupId();

    // 대상 미지정 → 그룹 ACTIVE 멤버 전체(요청자 제외)
    if (targets.isEmpty()) {
      List<Long> members = groupMemberRepository.findMemberIdsByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
      if (members == null || members.isEmpty()) return List.of();

      // 요청자 제외
      members.removeIf(m -> Objects.equals(m, req.getRequesterId()));

      List<AssignmentOverride> created = new ArrayList<>();
      for (Long m : members) {
        // 같은 그룹 멤버만 허용
        assertInSameGroup(a, m);
        // 중복 대기요청은 스킵
        if (overrideRepository.existsByAssignment_IdAndTargetIdAndStatus(a.getId(), m, OverrideStatus.REQUESTED)) {
          continue;
        }
        created.add(overrideRepository.save(
            AssignmentOverride.builder()
                .assignment(a)
                .requesterId(req.getRequesterId())
                .targetId(m)
                .status(OverrideStatus.REQUESTED)
                .build()
        ));
      }
      notifyBoard(a, req.getRequesterId(), new HashSet<>(members), false);
      return AssignmentOverrideResponse.fromAll(created);
    }

    // 대상 지정/다중 지정
    List<AssignmentOverride> created = new ArrayList<>();
    for (Long t : targets) {
      assertInSameGroup(a, t);
      if (overrideRepository.existsByAssignment_IdAndTargetIdAndStatus(a.getId(), t, OverrideStatus.REQUESTED)) {
        continue;
      }
      created.add(overrideRepository.save(
          AssignmentOverride.builder()
              .assignment(a)
              .requesterId(req.getRequesterId())
              .targetId(t)
              .status(OverrideStatus.REQUESTED)
              .build()
      ));
    }
    notifyBoard(a, req.getRequesterId(), targets, false);
    return AssignmentOverrideResponse.fromAll(created);
  }

  // ========== 응답(수락/거절) ==========
  @Transactional
  public AssignmentOverrideResponse respondToOverrideRequest(Long requestId, AssignmentOverrideStatusUpdateRequest req) {
    if (req.getActorMemberId() == null || req.getStatus() == null)
      throw new CustomException(ErrorCode.INVALID_REQUEST);

    AssignmentOverride r = overrideRepository.findById(requestId)
        .orElseThrow(() -> new CustomException(ErrorCode.OVERRIDE_REQUEST_NOT_FOUND));

    TaskAssignment a = assignmentRepository.findByIdForUpdate(r.getAssignment().getId())
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    assertTodayOrFuture(a.getDate());
    assertRequested(r);

    final Long actor = req.getActorMemberId();
    assertInSameGroup(a, actor);

    // ----- ACCEPT -----
    if (req.getStatus() == OverrideStatus.ACCEPTED) {
      // 스왑: 대상자만 수락 가능
      if (r.getSwapAssignmentId() != null) {
        // 교착방지: ID 오름차순으로 락
        Long id1 = Math.min(a.getId(), r.getSwapAssignmentId());
        Long id2 = Math.max(a.getId(), r.getSwapAssignmentId());
        TaskAssignment first  = assignmentRepository.findByIdForUpdate(id1)
            .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));
        TaskAssignment second = assignmentRepository.findByIdForUpdate(id2)
            .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

        assertTodayOrFuture(first.getDate());
        assertTodayOrFuture(second.getDate());
        assertSameGroup(first, second);

        if (!Objects.equals(r.getTargetId(), actor))
          throw new CustomException(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET);

        Long tmp = first.getGroupMemberId();
        first.setGroupMemberId(second.getGroupMemberId());
        second.setGroupMemberId(tmp);

        r.setStatus(OverrideStatus.ACCEPTED);
        r.setModifierId(actor);

        // 두 배정의 남은 대기요청 일괄 거절
        overrideRepository.bulkUpdateStatusByAssignmentId(first.getId(),  OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor);
        overrideRepository.bulkUpdateStatusByAssignmentId(second.getId(), OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor);

        // 히스토리 기록 추가
        overrideHistoryService.record(r, r.getTargetId(), actor, 0L);

        return AssignmentOverrideResponse.from(r);
      }

      // 일반(브로드캐스트 분해된) 지정요청: 대상자만 수락 가능
      if (!Objects.equals(r.getTargetId(), actor))
        throw new CustomException(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET);

      // 담당자 변경
      a.setGroupMemberId(actor);
      r.setStatus(OverrideStatus.ACCEPTED);
      r.setModifierId(actor);

      // 같은 배정의 다른 대기요청 모두 거절
      overrideRepository.bulkUpdateStatusByAssignmentId(
          a.getId(), OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor
      );

      // 히스토리 기록 추가
      overrideHistoryService.record(r, r.getTargetId(), actor, 0L);

      return AssignmentOverrideResponse.from(r);
    }

    // ----- REJECT -----
    // 대상자만 거절 가능
    if (!Objects.equals(r.getTargetId(), actor))
      throw new CustomException(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET);

    r.setStatus(OverrideStatus.REJECTED);
    r.setModifierId(actor);

    // 모두 거절됐는지 확인 (요청 상태가 더 이상 없으면 '전체 실패' 상태)
    List<AssignmentOverride> remainRequested =
        overrideRepository.findAllByAssignment_IdAndStatus(a.getId(), OverrideStatus.REQUESTED);
    if (remainRequested.isEmpty()) {
    }

    return AssignmentOverrideResponse.from(r);
  }

  // ===== Helpers =====
  private void assertTodayOrFuture(LocalDate date) {
    if (date.isBefore(LocalDate.now()))
      throw new CustomException(ErrorCode.OVERRIDE_PAST_DATE_FORBIDDEN);
  }

  private void assertCurrentAssignee(TaskAssignment a, Long requesterId) {
    if (!Objects.equals(a.getGroupMemberId(), requesterId))
      throw new CustomException(ErrorCode.OVERRIDE_REQUESTER_MUST_BE_ASSIGNEE);
  }

  private void assertRequested(AssignmentOverride r) {
    if (r.getStatus() != OverrideStatus.REQUESTED)
      throw new CustomException(ErrorCode.OVERRIDE_ALREADY_PROCESSED);
  }

  private Set<Long> collectTargets(AssignmentOverrideRequest req) {
    Set<Long> targets = new LinkedHashSet<>();
    if (req.getTargetId() != null) targets.add(req.getTargetId());
    if (req.getTargetIds() != null && !req.getTargetIds().isEmpty()) targets.addAll(req.getTargetIds());
    return targets;
  }

  private void notifyBoard(TaskAssignment a, Long requesterId, Set<Long> targets, boolean swap) {
    if (boardApiUrl == null || boardApiUrl.isBlank()) return;
    try {
      Long groupId = a.getTemplate().getGroupId();
      String title = (swap ? "[서로 변경 요청] " : "[대신 해줄 사람 찾기] ") + a.getDate() + " 담당 교체 요청";
      String content = "assignmentId=" + a.getId() + ", requester=" + requesterId + ", targets=" + targets;
      Map<String, Object> payload = new HashMap<>();
      payload.put("groupId", groupId);
      payload.put("title", title);
      payload.put("content", content);
      new RestTemplate().postForObject(boardApiUrl + "/posts/auto", payload, Void.class);
    } catch (Exception ignore) { }
  }

  private void assertInSameGroup(TaskAssignment a, Long groupMemberId) {
    Long gid = a.getTemplate().getGroupId();
    boolean ok = groupMemberRepository.existsByGroupIdAndMemberId(gid, groupMemberId);
    if (!ok) throw new CustomException(ErrorCode.OVERRIDE_NOT_SAME_GROUP);
  }

  private void assertSameGroup(TaskAssignment a1, TaskAssignment a2) {
    Long g1 = a1.getTemplate().getGroupId();
    Long g2 = a2.getTemplate().getGroupId();
    if (!Objects.equals(g1, g2))
      throw new CustomException(ErrorCode.OVERRIDE_SWAP_DIFFERENT_GROUP);
  }
}
