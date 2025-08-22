package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideRequest;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideStatusUpdateRequest;
import com.zero.cohousesever.tasks.entity.AssignmentOverride;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import com.zero.cohousesever.tasks.repository.AssignmentOverrideRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
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

  private static final long BROADCAST_TARGET = 0L; // 전체요청 센티널

  private final AssignmentOverrideRepository overrideRepository;
  private final TaskAssignmentRepository assignmentRepository;

  // 게시판 자동 알림
  @Value("${board.api.url:}")
  private String boardApiUrl;

  // ========== 생성 ==========
  @Transactional
  public List<AssignmentOverrideResponse> createOverrideRequests(Long assignmentId, AssignmentOverrideRequest req) {
    if (req.getRequesterId() == null) throw new CustomException(ErrorCode.INVALID_REQUEST);

    TaskAssignment a = assignmentRepository.findByIdForUpdate(assignmentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    assertTodayOrFuture(a.getDate());
    assertCurrentAssignee(a, req.getRequesterId());

    // 스왑 우선 처리 (상대 배정의 현재 담당자에게 지정요청)
    if (req.getSwapAssignmentId() != null) {
      TaskAssignment other = assignmentRepository.findByIdForUpdate(req.getSwapAssignmentId())
          .orElseThrow(() -> new CustomException(ErrorCode.OVERRIDE_SWAP_TARGET_NOT_FOUND));

      assertTodayOrFuture(other.getDate());
      assertSameGroup(a, other);

      Long targetId = other.getGroupMemberId();
      assertInSameGroup(a, targetId);

      if (overrideRepository.existsByAssignment_IdAndTargetIdAndStatus(a.getId(), targetId, OverrideStatus.REQUESTED)) {
        // 중복이면 알림만 남기고 종료(필요시 CONFLICT로 바꿀 수 있음)
        notifyBoard(a, req.getRequesterId(), Set.of(targetId), true);
        return List.of();
      }

      AssignmentOverride saved = overrideRepository.save(
          AssignmentOverride.builder()
              .assignment(a)                         // 내 배정
              .requesterId(req.getRequesterId())     // 현 담당
              .targetId(targetId)                    // 상대 담당
              .swapAssignmentId(other.getId())       // 스왑 대상 저장
              .status(OverrideStatus.REQUESTED)
              .build()
      );
      notifyBoard(a, req.getRequesterId(), Set.of(targetId), true);
      return List.of(AssignmentOverrideResponse.from(saved));
    }

    // 지정/다중/전체
    Set<Long> targets = collectTargets(req);
    List<AssignmentOverride> created = new ArrayList<>();

    if (targets.isEmpty()) {
      // 브로드캐스트(전체요청)
      boolean exists = overrideRepository.existsByAssignment_IdAndTargetIdAndStatus(
          a.getId(), BROADCAST_TARGET, OverrideStatus.REQUESTED);
      if (!exists) {
        created.add(overrideRepository.save(
            AssignmentOverride.builder()
                .assignment(a)
                .requesterId(req.getRequesterId())
                .targetId(BROADCAST_TARGET)
                .status(OverrideStatus.REQUESTED)
                .build()
        ));
      }
    } else {
      for (Long t : targets) {
        // 같은 그룹 멤버만 허용
        assertInSameGroup(a, t);

        // 중복 대기요청은 스킵
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
    }

    // 게시판 알림
    notifyBoard(a, req.getRequesterId(), targets.isEmpty() ? null : targets, false);

    return AssignmentOverrideResponse.fromAll(created);
  }

  // ========== 응답(수락/거절) ==========
  @Transactional
  public AssignmentOverrideResponse respondToOverrideRequest(Long requestId, AssignmentOverrideStatusUpdateRequest req) {
    if (req.getGroupMemberId() == null || req.getStatus() == null)
      throw new CustomException(ErrorCode.INVALID_REQUEST);

    AssignmentOverride r = overrideRepository.findById(requestId)
        .orElseThrow(() -> new CustomException(ErrorCode.OVERRIDE_REQUEST_NOT_FOUND));

    TaskAssignment a = assignmentRepository.findByIdForUpdate(r.getAssignment().getId())
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    assertTodayOrFuture(a.getDate());
    assertRequested(r);

    final Long actor = req.getGroupMemberId();
    assertInSameGroup(a, actor);

    if (req.getStatus() == OverrideStatus.ACCEPTED) {
      // 스왑 수락: 두 배정의 담당자를 맞교환
      if (r.getSwapAssignmentId() != null) {
        // 교착방지: ID 오름차순으로 락 획득
        Long id1 = Math.min(a.getId(), r.getSwapAssignmentId());
        Long id2 = Math.max(a.getId(), r.getSwapAssignmentId());
        TaskAssignment first  = assignmentRepository.findByIdForUpdate(id1)
            .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));
        TaskAssignment second = assignmentRepository.findByIdForUpdate(id2)
            .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

        assertTodayOrFuture(first.getDate());
        assertTodayOrFuture(second.getDate());
        assertSameGroup(first, second);

        // 지정요청은 대상자만 수락 가능
        if (!Objects.equals(r.getTargetId(), actor))
          throw new CustomException(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET);

        Long tmp = first.getGroupMemberId();
        first.setGroupMemberId(second.getGroupMemberId());
        second.setGroupMemberId(tmp);

        r.setStatus(OverrideStatus.ACCEPTED);
        r.setModifierId(actor);

        // 양쪽 배정의 다른 대기요청 일괄 거절
        overrideRepository.bulkUpdateStatusByAssignmentId(first.getId(),  OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor);
        overrideRepository.bulkUpdateStatusByAssignmentId(second.getId(), OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor);

        return AssignmentOverrideResponse.from(r);
      }

      // 단일 교체 수락: 브로드캐스트는 누구나, 지정요청은 대상자만
      if (!isBroadcast(r.getTargetId()) && !Objects.equals(r.getTargetId(), actor))
        throw new CustomException(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET);

      if (isBroadcast(r.getTargetId())) r.setTargetId(actor); // 수락자 기록
      a.setGroupMemberId(actor);                               // 담당자 변경
      r.setStatus(OverrideStatus.ACCEPTED);
      r.setModifierId(actor);

      // 같은 배정의 나머지 대기요청 일괄 거절
      overrideRepository.bulkUpdateStatusByAssignmentId(
          a.getId(), OverrideStatus.REQUESTED, OverrideStatus.REJECTED, actor
      );
      return AssignmentOverrideResponse.from(r);
    }

    // ===== REJECTED =====
    // 브로드캐스트(전체요청)는 모두가 계속 볼 수 있어야 하므로 유지
    if (isBroadcast(r.getTargetId()))
      throw new CustomException(ErrorCode.OVERRIDE_BROADCAST_REJECT_FORBIDDEN);

    // 지정요청 거절은 대상자만 가능
    if (!Objects.equals(r.getTargetId(), actor))
      throw new CustomException(ErrorCode.OVERRIDE_ACCEPTOR_MUST_BE_TARGET);

    r.setStatus(OverrideStatus.REJECTED);
    r.setModifierId(actor);
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

  private boolean isBroadcast(Long targetId) {
    return Objects.equals(targetId, BROADCAST_TARGET);
  }

  private Set<Long> collectTargets(AssignmentOverrideRequest req) {
    Set<Long> targets = new LinkedHashSet<>();
    if (req.getTargetId() != null) targets.add(req.getTargetId());
    if (req.getTargetIds() != null && !req.getTargetIds().isEmpty()) targets.addAll(req.getTargetIds());
    return targets;
  }

  // 게시판 자동 알림 (환경변수 없으면 NO-OP, 존재하면 REST 호출)
  private void notifyBoard(TaskAssignment a, Long requesterId, Set<Long> targets, boolean swap) {
    if (boardApiUrl == null || boardApiUrl.isBlank()) return;
    try {
      Long groupId = resolveGroupId(a);
      String title = (swap ? "[서로 변경 요청] " : "[대신 해줄 사람 찾기] ") + a.getDate() + " 담당 교체 요청";
      String content = "assignmentId=" + a.getId() + ", requester=" + requesterId + ", targets=" + targets;
      Map<String, Object> payload = new HashMap<>();
      payload.put("groupId", groupId);
      payload.put("title", title);
      payload.put("content", content);
      new RestTemplate().postForObject(boardApiUrl + "/posts/auto", payload, Void.class);
    } catch (Exception ignore) { }
  }

  // 템플릿에서 groupId 얻기
  private Long resolveGroupId(TaskAssignment a) {
    return a.getTemplate().getGroupId();
  }

  // 같은 그룹 멤버 검증
  private void assertInSameGroup(TaskAssignment a, Long groupMemberId) {
    Long gid = resolveGroupId(a);
    boolean ok = assignmentRepository.existsByTemplate_GroupIdAndGroupMemberId(gid, groupMemberId);
    if (!ok) throw new CustomException(ErrorCode.OVERRIDE_NOT_SAME_GROUP);
  }

  // 두 배정이 같은 그룹인지 검증
  private void assertSameGroup(TaskAssignment a1, TaskAssignment a2) {
    Long g1 = resolveGroupId(a1);
    Long g2 = resolveGroupId(a2);
    if (!Objects.equals(g1, g2))
      throw new CustomException(ErrorCode.OVERRIDE_SWAP_DIFFERENT_GROUP);
  }
}
