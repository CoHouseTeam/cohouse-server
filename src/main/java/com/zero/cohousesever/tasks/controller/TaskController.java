package com.zero.cohousesever.tasks.controller;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.group.repository.GroupRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentStatusUpdateRequest;
import com.zero.cohousesever.tasks.dto.assignment.UncompletedByMemberResponse;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideRequest;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideResponse;
import com.zero.cohousesever.tasks.dto.override.AssignmentOverrideStatusUpdateRequest;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayResponse;
import com.zero.cohousesever.tasks.dto.template.TaskTemplateRequest;
import com.zero.cohousesever.tasks.dto.template.TaskTemplateResponse;
import com.zero.cohousesever.tasks.dto.template.TaskTemplateUpdateRequest;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.AssignmentOverrideRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import com.zero.cohousesever.tasks.service.AssignmentOverrideHistoryService;
import com.zero.cohousesever.tasks.service.AssignmentOverrideService;
import com.zero.cohousesever.tasks.service.RepeatDayService;
import com.zero.cohousesever.tasks.service.TaskAssignmentHistoryService;
import com.zero.cohousesever.tasks.service.TaskAssignmentService;
import com.zero.cohousesever.tasks.service.TaskTemplateService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskTemplateService taskTemplateService;
  private final RepeatDayService repeatDayService;
  private final TaskAssignmentService taskAssignmentService;
  private final AssignmentOverrideService assignmentOverrideService;
  private final TaskAssignmentRepository taskAssignmentRepository;
  private final AssignmentOverrideRepository assignmentOverrideRepository;
  private final TaskAssignmentHistoryService taskAssignmentHistoryService;
  private final AssignmentOverrideHistoryService assignmentOverrideHistoryService;

  private final MemberRepository memberRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;

  // 그룹장 여부
  private void ensureLeader(Long memberId, Long groupId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Group group = groupRepository.findById(groupId)
        .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

    if (!groupMemberRepository.existsByGroupAndMemberAndIsLeaderTrue(group, member)) {
      throw new CustomException(ErrorCode.NOT_GROUP_LEADER);
    }
  }

  //그룹원 확인
  private void ensureMember(Long memberId, Long groupId) {
    if (!groupMemberRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
      throw new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
  }

  // MemberId, GroupMemberId 오류로 인한 정규화
  private Long normalizeToMemberId(Long groupId, Long maybeMemberOrGroupMemberId) {
    // 이미 memberId인지 빠른 체크
    if (groupMemberRepository.existsByGroupIdAndMemberId(groupId, maybeMemberOrGroupMemberId)) {
      return maybeMemberOrGroupMemberId; // memberId
    }
    // group_member.id로 역조회 → memberId 추출
    return groupMemberRepository.findById(maybeMemberOrGroupMemberId)
        .filter(gm -> gm.getGroup().getId().equals(groupId))
        .map(gm -> gm.getMember().getId())
        .orElseThrow(() -> new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
  }

  private List<Long> normalizeToMemberIds(Long groupId, List<Long> ids) {
    if (ids == null) {
      return List.of();
    }
    return ids.stream().map(id -> normalizeToMemberId(groupId, id)).toList();
  }

  // 1. 템플릿 관련

  // 조회
  @GetMapping("/templates")
  public ResponseEntity<?> getTemplates(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long groupId
  ) {
    ensureMember(user.getId(), groupId);
    List<TaskTemplateResponse> body = taskTemplateService.getAllTemplates(groupId).stream()
        .map(TaskTemplateResponse::from)
        .toList();
    return ResponseEntity.ok(body);
  }

  // 생성
  @PostMapping("/templates")
  public ResponseEntity<TaskTemplateResponse> createTemplate(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody TaskTemplateRequest request) {

    ensureLeader(user.getId(), request.getGroupId());

    TaskTemplate saved = taskTemplateService.createTemplate(
        request.getGroupId(), request.getCategory(), request.getRepeatDays(),
        request.getRandomEnabled());
    return ResponseEntity.ok(TaskTemplateResponse.from(saved));
  }


  // 수정
  @PutMapping("/templates/{templateId}")
  public ResponseEntity<TaskTemplateResponse> updateTemplate(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long templateId,
      @RequestBody TaskTemplateUpdateRequest request) {

    // 템플릿 -> groupId 먼저 알아와서 체크
    TaskTemplate t = taskTemplateService.getById(templateId); // 없으면 내부에서 예외
    ensureLeader(user.getId(), t.getGroupId());

    TaskTemplate updated = taskTemplateService.updateTemplate(templateId, request.getCategory());
    return ResponseEntity.ok(TaskTemplateResponse.from(updated));
  }

  // 삭제
  @DeleteMapping("/templates/{templateId}")
  public ResponseEntity<Void> deleteTemplate(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long templateId) {

    TaskTemplate t = taskTemplateService.getById(templateId);
    ensureLeader(user.getId(), t.getGroupId());

    taskTemplateService.deleteTemplate(templateId);
    return ResponseEntity.noContent().build();
  }

  // 2. 반복 요일 관련

  // 조회
  @GetMapping("/templates/{templateId}/repeat-days")
  public ResponseEntity<List<RepeatDayResponse>> getRepeatDays(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long templateId
  ) {
    Long groupId = taskTemplateService.getGroupIdByTemplateId(templateId);
    ensureMember(user.getId(), groupId);
    return ResponseEntity.ok(repeatDayService.getRepeatDaysByTemplateId(templateId));
  }

  // 생성
  @PostMapping("/templates/{templateId}/repeat-days")
  public ResponseEntity<RepeatDayResponse> addRepeatDay(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long templateId,
      @RequestBody RepeatDayRequest request) {

    TaskTemplate t = taskTemplateService.getById(templateId);
    ensureLeader(user.getId(), t.getGroupId());

    return ResponseEntity.ok(repeatDayService.addRepeatDay(templateId, request));
  }

  // 수정
  @PutMapping("/templates/{templateId}/repeat-days/{repeatDayId}")
  public ResponseEntity<RepeatDayResponse> updateRepeatDay(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long templateId,
      @PathVariable Long repeatDayId,
      @RequestBody RepeatDayRequest request
  ) {
    TaskTemplate t = taskTemplateService.getById(templateId);
    ensureLeader(user.getId(), t.getGroupId());

    RepeatDayResponse updated = repeatDayService.updateRepeatDay(templateId, repeatDayId, request);
    return ResponseEntity.ok(updated);
  }


  // 삭제
  @DeleteMapping("/templates/{templateId}/repeat-days/{repeatDayId}")
  public ResponseEntity<Void> deleteRepeatDay(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long templateId,
      @PathVariable Long repeatDayId) {

    TaskTemplate t = taskTemplateService.getById(templateId);
    ensureLeader(user.getId(), t.getGroupId());

    repeatDayService.deleteRepeatDay(templateId, repeatDayId);
    return ResponseEntity.noContent().build();
  }

  // 3. 할일 배정 관련

  // 조회
  @GetMapping("/assignments")
  public ResponseEntity<List<TaskAssignmentResponse>> getAssignments(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long groupId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) Long memberId
  ) {
    ensureMember(user.getId(), groupId);

    if (memberId != null &&
        !groupMemberRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
      throw new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
    return ResponseEntity.ok(taskAssignmentService.getAssignments(groupId, from, to, memberId));
  }

  // 생성
  @PostMapping("/assignments")
  public ResponseEntity<TaskAssignmentResponse> assignTask(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody TaskAssignmentRequest request) {

    ensureLeader(user.getId(), request.getGroupId());

    // 후보와 고정 배정자만 memberId로 정규화
    if (request.getGroupMemberId() != null) {
      request.setGroupMemberId(
          normalizeToMemberIds(request.getGroupId(), request.getGroupMemberId()));
    }
    if (request.getFixedAssigneeId() != null) {
      request.setFixedAssigneeId(
          normalizeToMemberId(request.getGroupId(), request.getFixedAssigneeId()));
    }
    var created = taskAssignmentService.assignTaskManuallyOrRandomly(request);
    if (created == null || created.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(created.get(0));
  }

  // 할 일 상태 변경
  @PutMapping("/assignments/{assignmentId}")
  public ResponseEntity<TaskAssignmentResponse> updateAssignmentStatus(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long assignmentId,
      @RequestBody TaskAssignmentStatusUpdateRequest request
  ) {
    var a = taskAssignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    Long groupId = a.getTemplate().getGroupId();
    Long assigneeId = a.getGroupMemberId();

    ensureMember(user.getId(), groupId);

    if (!user.getId().equals(assigneeId)) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    var body = taskAssignmentService.updateAssignmentStatus(assignmentId, request.getStatus());
    return ResponseEntity.ok(body);
  }

  // 할 일 미이행 리스트
  @GetMapping("/assignments/uncompleted")
  public ResponseEntity<List<TaskAssignmentResponse>> getUncompletedThisWeek(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long groupId,
      @RequestParam(required = false) Long memberId
  ) {
    ensureMember(user.getId(), groupId);
    if (memberId != null &&
        !groupMemberRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
      throw new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
    return ResponseEntity.ok(taskAssignmentService.getUncompletedThisWeek(groupId, memberId));
  }

  @GetMapping("/assignments/uncompleted/by-member")
  public ResponseEntity<List<UncompletedByMemberResponse>> getUncompletedThisWeekByMember(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam Long groupId
  ) {
    ensureMember(user.getId(), groupId);
    return ResponseEntity.ok(taskAssignmentService.getUncompletedThisWeekByMember(groupId));
  }

  // 4. 담당자 변경 요청 관련

  // 요청 생성
  @PostMapping("/assignments/{assignmentId}/override-request")
  public ResponseEntity<List<AssignmentOverrideResponse>> requestOverride(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long assignmentId,
      @RequestBody AssignmentOverrideRequest request
  ) {
    var a = taskAssignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));
    Long groupId = a.getTemplate().getGroupId();

    ensureMember(user.getId(), groupId);

    request.setRequesterId(user.getId());

    // 대상자 단일/다중 정규화 (있으면)
    if (request.getTargetId() != null) {
      request.setTargetId(normalizeToMemberId(groupId, request.getTargetId()));
    }
    if (request.getTargetIds() != null && !request.getTargetIds().isEmpty()) {
      request.setTargetIds(normalizeToMemberIds(groupId, request.getTargetIds()));
    }

    var created = assignmentOverrideService.createOverrideRequests(assignmentId, request);
    return ResponseEntity.ok(created);
  }

  // 요청 수락/거절
  @PatchMapping("/override-requests/{requestId}")
  public ResponseEntity<AssignmentOverrideResponse> respondOverride(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long requestId,
      @RequestBody AssignmentOverrideStatusUpdateRequest request
  ) {
    var r = assignmentOverrideRepository.findById(requestId)
        .orElseThrow(() -> new CustomException(ErrorCode.OVERRIDE_REQUEST_NOT_FOUND));
    Long groupId = r.getAssignment().getTemplate().getGroupId();

    ensureMember(user.getId(), groupId);

    Long normalizedActorId = normalizeToMemberId(groupId, request.getActorMemberId());
    request.setActorMemberId(normalizedActorId);

    var updated = assignmentOverrideService.respondToOverrideRequest(requestId, request);
    return ResponseEntity.ok(updated);
  }

  // 할일 이행 히스토리 조회
  @GetMapping("/assignments/{assignmentId}/histories")
  public ResponseEntity<List<TaskAssignmentResponse>> getTaskAssignmentHistories(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long assignmentId
  ) {
    var a = taskAssignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    Long groupId = a.getTemplate().getGroupId();
    ensureMember(user.getId(), groupId);

    return ResponseEntity.ok(taskAssignmentHistoryService.getAssignmentHistories(assignmentId));
  }

  // 담당자 변경 요청 히스토리 조회
  @GetMapping("/override-requests/{requestId}/histories")
  public ResponseEntity<List<AssignmentOverrideResponse>> getAssignmentOverrideHistories(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable Long requestId
  ) {
    var r = assignmentOverrideRepository.findById(requestId)
        .orElseThrow(() -> new CustomException(ErrorCode.OVERRIDE_REQUEST_NOT_FOUND));

    Long groupId = r.getAssignment().getTemplate().getGroupId();
    ensureMember(user.getId(), groupId);

    return ResponseEntity.ok(assignmentOverrideHistoryService.getOverrideHistories(requestId));
  }


}


