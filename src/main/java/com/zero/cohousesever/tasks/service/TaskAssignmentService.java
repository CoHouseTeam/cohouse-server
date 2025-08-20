package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 실제 주간/일간 할일 배정 및 스케줄 생성, 조회 등 담당
 */
@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

  private final TaskAssignmentRepository taskAssignmentRepository;
  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayRepository repeatDayRepository;

  /**
   * 템플릿의 반복요일을 확인한 뒤, 후보 목록(그룹 멤버 ID들) 중
   * "해당 주의 작업량(담당 템플릿 수)이 가장 적은 멤버"를 골라,
   * 그 주의 모든 반복요일에 동일 담당자로 배정한다.
   * 이미 같은 (templateId, date)이 존재하면 생성하지 않고 스킵한다.
   */
  public List<TaskAssignmentResponse> assignTaskManuallyOrRandomly(TaskAssignmentRequest req) {

    // 0) 기본 검증
    if (req.getTemplateId() == null || req.getGroupId() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    List<Long> candidateIds = req.getGroupMemberId(); // DTO 변경 반영
    if (candidateIds == null || candidateIds.isEmpty()) {
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

    // 3) 기준 주 계산: 입력 없으면 오늘 기준 '다음 주', 있으면 파싱 후 '다음 주'
    final LocalDate base;
    try {
      base = (req.getDate() == null || req.getDate().isBlank())
          ? LocalDate.now(ZoneId.of("Asia/Seoul")).plusWeeks(1)
          : LocalDate.parse(req.getDate()).plusWeeks(1);
    } catch (DateTimeParseException e) {
      throw new CustomException(ErrorCode.DATE_FORMAT_INVALID);
    }
    LocalDate sunday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate weekStart = sunday;
    LocalDate weekEnd   = sunday.plusDays(6);

    // 3-1) 해당 템플릿의 기존 배정들 중 이번 주만 골라 중복 날짜 Skip 용 집합 구성
    List<TaskAssignment> existingAll = taskAssignmentRepository.findByTemplate_Id(req.getTemplateId());
    Set<LocalDate> alreadyInWeek = existingAll.stream()
        .map(TaskAssignment::getDate)
        .filter(d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd)) // weekStart <= d <= weekEnd
        .collect(Collectors.toSet());

    // 4) 이번 주 작업량(담당 템플릿 수)
    Long pickedMemberId = pickMemberByWeeklyLoad(req.getGroupId(), weekStart, weekEnd, candidateIds);

    // 5) 생성
    List<TaskAssignment> toSave = new ArrayList<>();
    for (RepeatDay rd : days) {
      LocalDate d = sunday.plusDays(rd.getDayOfWeek().getValue() % 7); // SUN=0
      if (alreadyInWeek.contains(d)) continue; // 중복 skip
      toSave.add(TaskAssignment.builder()
          .template(template)
          .groupMemberId(pickedMemberId)
          .date(d)
          .build()); // @PrePersist → PENDING
    }

    if (toSave.isEmpty()) return List.of();
    return TaskAssignmentResponse.fromAll(taskAssignmentRepository.saveAll(toSave));
  }

  // 후보들 중 이번 주(weekStart~weekEnd) 작업량(담당 템플릿 수)이 가장 적은 멤버 선택 (동률 랜덤)
  private Long pickMemberByWeeklyLoad(Long groupId, LocalDate weekStart, LocalDate weekEnd, List<Long> candidates) {
    // 이번 주, 해당 그룹의 모든 배정 조회 (모든 템플릿 포함)
    List<TaskAssignment> weeklyAll =
        taskAssignmentRepository.findByTemplate_GroupIdAndDateBetween(groupId, weekStart, weekEnd);

    // 멤버별 "이번 주 맡은 템플릿 id 집합" → 템플릿 수가 작업량
    Map<Long, Set<Long>> memberToTemplateSet = new HashMap<>();
    for (TaskAssignment a : weeklyAll) {
      Long mId = a.getGroupMemberId();
      if (mId == null) continue;
      memberToTemplateSet.computeIfAbsent(mId, k -> new HashSet<>())
          .add(a.getTemplate().getId());
    }

    List<Long> tied = new ArrayList<>();
    int best = Integer.MAX_VALUE;
    for (Long c : candidates) {
      int load = memberToTemplateSet.getOrDefault(c, java.util.Collections.emptySet()).size();
      if (load < best) {
        best = load;
        tied.clear();
        tied.add(c);
      } else if (load == best) {
        tied.add(c);
      }
    }
    int idx = ThreadLocalRandom.current().nextInt(tied.size());
    return tied.get(idx);
  }

  /**
   * 할일 배정 목록 조회 (최소 단위: 주)
   * - from/to가 비면 이번 주(월~일)
   * - 한쪽만 오면 그 날짜의 주(월~일)
   * - 둘 다 오면 from의 월요일 ~ to의 일요일 (여러 주 포함 가능)
   * - memberId가 있으면 해당 멤버만, 없으면 전체
   */
  public List<TaskAssignmentResponse> getAssignments(
      Long groupId,
      LocalDate from,  // 기간 시작(옵션)
      LocalDate to,    // 기간 종료(옵션)
      Long memberId    // 멤버 ID(옵션)
  ) {
    if (groupId == null) throw new CustomException(ErrorCode.INVALID_REQUEST);

    // 1) 주간 범위 계산 (월~일로 스냅; 둘 다 null이면 이번 주)
    LocalDate[] range = computeWeekRange(from, to);
    LocalDate start = range[0], end = range[1];

    // 2) 레포 호출 (전체/멤버)
    List<TaskAssignment> list = (memberId == null)
        ? taskAssignmentRepository.findByTemplate_GroupIdAndDateBetween(groupId, start, end)
        : taskAssignmentRepository.findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(groupId, memberId, start, end);

    // 3) 정렬(일요일 우선) + repeatType 매핑
    return list.stream()
        .sorted(Comparator
            .comparing((TaskAssignment a) -> a.getDate().getDayOfWeek().getValue() % 7)
            .thenComparing(TaskAssignment::getDate)
            .thenComparing(TaskAssignment::getId))
        .map(a -> {
          String repeatType = repeatDayRepository.existsByTaskTemplate_Id(a.getTemplate().getId()) ? "WEEKLY" : "NONE";
          return TaskAssignmentResponse.from(a, repeatType);
        })
        .collect(Collectors.toList());
  }

  /** 주간 범위 계산 규칙
   * - 둘 다 null → 이번 주(월~일)
   * - 한쪽만 주어지면 그 날짜의 주(월~일)
   * - 둘 다 주어지면: from의 월요일 ~ to의 일요일 (여러 주 포함 가능)
   */
  private static LocalDate[] computeWeekRange(LocalDate from, LocalDate to) {
    if (from == null && to == null) {
      LocalDate mon = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY);
      return new LocalDate[]{mon, mon.plusDays(6)};
    }
    if (from == null) from = to;
    if (to == null)   to   = from;
    if (to.isBefore(from)) { LocalDate tmp = from; from = to; to = tmp; }

    LocalDate start = from.with(DayOfWeek.MONDAY);
    LocalDate end   = to.with(DayOfWeek.MONDAY).plusDays(6);
    return new LocalDate[]{start, end};
  }
}
