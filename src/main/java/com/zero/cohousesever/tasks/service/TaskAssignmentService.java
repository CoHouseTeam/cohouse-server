package com.zero.cohousesever.tasks.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.dto.assignment.UncompletedByMemberResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.entity.enums.AssignmentStatus;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Collectors.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final TaskAssignmentRepository taskAssignmentRepository;
  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayRepository repeatDayRepository;
  private final GroupMemberRepository groupMemberRepository;

  /**
   * 후보 중 '과거 배정 전무' 멤버를 1순위로,
   * 그다음 '주간 부하(담당 템플릿 수) 최소' 기준 선택(동률 랜덤)
   */
  private Long pickMemberByPriority(Long groupId, LocalDate weekFrom, LocalDate weekTo, List<Long> candidates) {
    // Tier 분리: 과거 배정 없음 / 있음
    List<Long> tier1 = new ArrayList<>();
    List<Long> tier2 = new ArrayList<>();
    for (Long c : candidates) {
      boolean hasAnyHistory = taskAssignmentRepository.existsByTemplate_GroupIdAndGroupMemberId(groupId, c);
      if (!hasAnyHistory) tier1.add(c); else tier2.add(c);
    }
    List<Long> pool = !tier1.isEmpty() ? tier1 : tier2;

    // 이번 주 모든 배정을 기준으로 멤버별 담당 '템플릿 수' 계산
    List<TaskAssignment> weeklyAll =
        taskAssignmentRepository.findByTemplate_GroupIdAndDateBetween(groupId, weekFrom, weekTo);

    Map<Long, Set<Long>> memberToTemplateSet = new HashMap<>();
    for (TaskAssignment a : weeklyAll) {
      Long mId = a.getGroupMemberId();
      if (mId == null) continue;
      memberToTemplateSet.computeIfAbsent(mId, k -> new HashSet<>()).add(a.getTemplate().getId());
    }

    List<Long> tied = new ArrayList<>();
    int best = Integer.MAX_VALUE;
    for (Long c : pool) {
      int load = memberToTemplateSet.getOrDefault(c, Collections.emptySet()).size();
      if (load < best) {
        best = load; tied.clear(); tied.add(c);
      } else if (load == best) {
        tied.add(c);
      }
    }
    return tied.get(ThreadLocalRandom.current().nextInt(tied.size()));
  }

  /**
   * 템플릿 반복요일 기준으로 "이번 주(일~토)" 배정 생성.
   * - 수동 고정 배정: request.fixedAssigneeId가 있으면 해당 멤버로 강제 배정(그룹 소속 검증)
   * - 랜덤/직전담당자 유지: request.randomEnabled가 null이면 템플릿 설정(randomEnabled) 사용
   */
  public List<TaskAssignmentResponse> assignTaskManuallyOrRandomly(TaskAssignmentRequest req) {
    // 0) 기본 검증
    if (req.getTemplateId() == null || req.getGroupId() == null) {
      throw new CustomException(ErrorCode.GROUP_ID_REQUIRED);
    }
    // 고정 배정이 없을 때는 후보군 필수
    List<Long> candidateIds = req.getGroupMemberId();
    if (req.getFixedAssigneeId() == null && (candidateIds == null || candidateIds.isEmpty())) {
      throw new CustomException(ErrorCode.CANDIDATE_MEMBERS_REQUIRED);
    }

    // 1) 템플릿 + 그룹 소유 검증
    TaskTemplate template = taskTemplateRepository.findById(req.getTemplateId())
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
    if (!req.getGroupId().equals(template.getGroupId())) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    // 2) 반복 요일 확보 (없으면 생성 불가)
    List<RepeatDay> days = repeatDayRepository.findByTaskTemplate_Id(req.getTemplateId());
    if (days.isEmpty()) {
      throw new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND);
    }

    // 3) 기준일 이번 주
    final LocalDate input;
    try {
      input = (req.getDate() == null || req.getDate().isBlank())
          ? LocalDate.now(KST)
          : LocalDate.parse(req.getDate());
    } catch (DateTimeParseException e) {
      throw new CustomException(ErrorCode.DATE_FORMAT_INVALID);
    }
    LocalDate sunday   = input.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate weekFrom = sunday;
    LocalDate weekTo   = sunday.plusDays(6);

    // 4 이번 주 중복 날짜 Skip
    Set<LocalDate> alreadyInWeek = taskAssignmentRepository
        .findByTemplate_IdAndDateBetween(req.getTemplateId(), weekFrom, weekTo)
        .stream()
        .map(TaskAssignment::getDate)
        .collect(toSet());

    // 5) 담당자 결정
    Long pickedMemberId;

    // 5-1) 수동 고정 배정이 온 경우: 그룹 소속 검증 후 그대로 사용
    if (req.getFixedAssigneeId() != null) {
      Long fixed = req.getFixedAssigneeId();
      boolean memberOfGroup = groupMemberRepository.existsByGroupIdAndMemberId(req.getGroupId(), fixed);
      if (!memberOfGroup) {
        throw new CustomException(ErrorCode.OVERRIDE_NOT_SAME_GROUP);
      }
      pickedMemberId = fixed;

    } else {
      // 5-2) 자동 선택(랜덤/직전담당자 유지)
      boolean random = (req.getRandomEnabled() != null) ? req.getRandomEnabled() : template.isRandomEnabled();

      if (random) {
        pickedMemberId = pickMemberByPriority(req.getGroupId(), weekFrom, weekTo, candidateIds);
      } else {
        // 직전 담당자 유지(없거나 후보에 없으면 신규우선 + 주간부하 최소)
        TaskAssignment prev = taskAssignmentRepository
            .findTopByTemplate_IdAndDateLessThanOrderByDateDesc(req.getTemplateId(), weekFrom);
        if (prev != null && candidateIds.contains(prev.getGroupMemberId())) {
          pickedMemberId = prev.getGroupMemberId();
        } else {
          pickedMemberId = pickMemberByPriority(req.getGroupId(), weekFrom, weekTo, candidateIds);
        }
      }
    }

    // 6) 생성
    List<TaskAssignment> toSave = new ArrayList<>();
    for (RepeatDay rd : days) {
      LocalDate d = sunday.plusDays(rd.getDayOfWeek().getValue() % 7); // SUNDAY=0
      if (alreadyInWeek.contains(d)) continue;
      toSave.add(TaskAssignment.builder()
          .template(template)
          .groupMemberId(pickedMemberId)
          .date(d)
          .build());
    }

    if (toSave.isEmpty()) return List.of();
    return TaskAssignmentResponse.fromAll(taskAssignmentRepository.saveAll(toSave));
  }

  /**
   * 주간 범위 계산 (일~토)
   */
  private static LocalDate[] computeWeekRange(LocalDate from, LocalDate to) {
    if (from == null && to == null) {
      LocalDate sun = LocalDate.now(KST).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
      return new LocalDate[]{sun, sun.plusDays(6)};
    }
    if (from == null) from = to;
    if (to == null)   to   = from;
    if (to.isBefore(from)) { LocalDate tmp = from; from = to; to = tmp; }

    LocalDate start = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate end   = to.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    return new LocalDate[]{start, end};
  }

  /**
   * 할 일 상태변경(이행여부) + repeatType 포함 응답
   */
  public TaskAssignmentResponse updateAssignmentStatus(Long assignmentId, AssignmentStatus status) {
    if (status == null) throw new CustomException(ErrorCode.ASSIGNMENT_STATUS_REQUIRED);

    TaskAssignment a = taskAssignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TASK_ASSIGNMENT_NOT_FOUND));

    a.setStatus(status);
    TaskAssignment saved = taskAssignmentRepository.save(a);

    String repeatType = repeatDayRepository.existsByTaskTemplate_Id(saved.getTemplate().getId()) ? "WEEKLY" : "NONE";
    return TaskAssignmentResponse.from(saved, repeatType);
  }

  /**
   * 이번 주 미이행 목록: 전체 or 특정 멤버
   */

  private static LocalDate[] computeThisWeekRange() {
    LocalDate sun = LocalDate.now(KST).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    return new LocalDate[]{sun, sun.plusDays(6)};
  }

  public List<TaskAssignmentResponse> getUncompletedThisWeek(Long groupId, Long memberId) {
    if (groupId == null) throw new CustomException(ErrorCode.GROUP_ID_REQUIRED);

    LocalDate[] range = computeThisWeekRange();
    LocalDate start = range[0], end = range[1];

    List<TaskAssignment> list = (memberId == null)
        ? taskAssignmentRepository.findByTemplate_GroupIdAndDateBetweenAndStatusNot(
        groupId, start, end, AssignmentStatus.COMPLETED)
        : taskAssignmentRepository.findByTemplate_GroupIdAndGroupMemberIdAndDateBetweenAndStatusNot(
            groupId, memberId, start, end, AssignmentStatus.COMPLETED);

    Set<Long> templateIds = list.stream().map(a -> a.getTemplate().getId()).collect(toSet());
    Set<Long> weeklyTemplateIds = templateIds.isEmpty()
        ? Collections.emptySet()
        : repeatDayRepository.findTemplateIdsHavingRepeat(templateIds);

    return list.stream()
        .sorted(Comparator
            .comparing((TaskAssignment a) -> a.getDate().getDayOfWeek().getValue() % 7)
            .thenComparing(TaskAssignment::getDate)
            .thenComparing(TaskAssignment::getId))
        .map(a -> TaskAssignmentResponse.from(
            a, weeklyTemplateIds.contains(a.getTemplate().getId()) ? "WEEKLY" : "NONE"))
        .collect(toList());
  }

  // 이번 주 미이행을 '담당자별'로 묶어서 반환
  public List<UncompletedByMemberResponse> getUncompletedThisWeekByMember(Long groupId) {
    if (groupId == null) throw new CustomException(ErrorCode.GROUP_ID_REQUIRED);

    LocalDate[] range = computeThisWeekRange();
    LocalDate start = range[0], end = range[1];

    List<TaskAssignment> list =
        taskAssignmentRepository.findByTemplate_GroupIdAndDateBetweenAndStatusNot(
            groupId, start, end, AssignmentStatus.COMPLETED);

    Set<Long> templateIds = list.stream().map(a -> a.getTemplate().getId()).collect(toSet());
    Set<Long> weeklyTemplateIds = templateIds.isEmpty()
        ? Collections.emptySet()
        : repeatDayRepository.findTemplateIdsHavingRepeat(templateIds);

    Map<Long, List<TaskAssignmentResponse>> grouped = list.stream()
        .sorted(Comparator
            .comparing((TaskAssignment a) -> a.getDate().getDayOfWeek().getValue() % 7)
            .thenComparing(TaskAssignment::getDate)
            .thenComparing(TaskAssignment::getId))
        .map(a -> TaskAssignmentResponse.from(
            a, weeklyTemplateIds.contains(a.getTemplate().getId()) ? "WEEKLY" : "NONE"))
        .collect(groupingBy(TaskAssignmentResponse::getGroupMemberId, LinkedHashMap::new, toList()));

    return grouped.entrySet().stream()
        .map(e -> UncompletedByMemberResponse.builder()
            .groupMemberId(e.getKey())
            .count(e.getValue().size())
            .assignments(e.getValue())
            .build())
        .sorted(Comparator.comparingInt(UncompletedByMemberResponse::getCount).reversed()
            .thenComparing(UncompletedByMemberResponse::getGroupMemberId))
        .collect(toList());
  }

  /**
   * 할일 배정 목록 조회 (최소 단위: 주)
   * - from/to 없으면 이번 주(일~토)
   * - 한쪽만 주어지면 그 날짜의 주(일~토)
   * - 둘 다 주어지면: from의 일요일 ~ to의 토요일
   * - memberId가 있으면 해당 멤버만, 없으면 전체
   */
  public List<TaskAssignmentResponse> getAssignments(Long groupId, LocalDate from, LocalDate to, Long memberId) {
    if (groupId == null) throw new CustomException(ErrorCode.GROUP_ID_REQUIRED);

    LocalDate[] range = computeWeekRange(from, to);
    LocalDate start = range[0], end = range[1];

    List<TaskAssignment> list = (memberId == null)
        ? taskAssignmentRepository.findByTemplate_GroupIdAndDateBetween(groupId, start, end)
        : taskAssignmentRepository.findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(groupId, memberId, start, end);

    Set<Long> templateIds = list.stream().map(a -> a.getTemplate().getId()).collect(toSet());
    Set<Long> weeklyTemplateIds = templateIds.isEmpty()
        ? Collections.emptySet()
        : repeatDayRepository.findTemplateIdsHavingRepeat(templateIds);

    return list.stream()
        .sorted(Comparator
            .comparing((TaskAssignment a) -> a.getDate().getDayOfWeek().getValue() % 7) // SUNDAY first
            .thenComparing(TaskAssignment::getDate)
            .thenComparing(TaskAssignment::getId))
        .map(a -> {
          String repeatType = weeklyTemplateIds.contains(a.getTemplate().getId()) ? "WEEKLY" : "NONE";
          return TaskAssignmentResponse.from(a, repeatType);
        })
        .collect(toList());
  }
}
