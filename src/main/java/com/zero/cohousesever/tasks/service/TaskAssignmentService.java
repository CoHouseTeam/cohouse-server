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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

  private final TaskAssignmentRepository taskAssignmentRepository;
  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayRepository repeatDayRepository;

  // 생성: 이번 주(일→토) 기준. randomEnabled=true 이면 랜덤, 아니면 "직전 주 담당자 그대로"
  public List<TaskAssignmentResponse> assignTaskManuallyOrRandomly(TaskAssignmentRequest req) {
    // 0) 검증
    if (req.getTemplateId() == null || req.getGroupId() == null)
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    List<Long> candidates = req.getGroupMemberId();
    if (candidates == null || candidates.isEmpty())
      throw new CustomException(ErrorCode.CANDIDATE_MEMBERS_REQUIRED);

    // 1) 템플릿 + 그룹 소유 검증
    TaskTemplate template = taskTemplateRepository.findById(req.getTemplateId())
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
    if (!req.getGroupId().equals(template.getGroupId()))
      throw new CustomException(ErrorCode.INVALID_REQUEST);

    // 2) 반복요일 확보
    List<RepeatDay> days = repeatDayRepository.findByTaskTemplate_Id(req.getTemplateId());
    if (days.isEmpty())
      throw new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND);

    // 3) 기준 주 계산 (일→토). 기본: 이번 주부터 적용
    final boolean applyThisWeek = (req.getApplyThisWeek() == null) || req.getApplyThisWeek();
    LocalDate base;
    try {
      base = (req.getDate() == null || req.getDate().isBlank())
          ? LocalDate.now(ZoneId.of("Asia/Seoul"))
          : LocalDate.parse(req.getDate());
    } catch (DateTimeParseException e) {
      throw new CustomException(ErrorCode.DATE_FORMAT_INVALID);
    }
    if (!applyThisWeek) base = base.plusWeeks(1); // 옵션 유지: false면 다음 주로 밀기
    LocalDate sunday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate weekStart = sunday;
    LocalDate weekEnd   = sunday.plusDays(6);

    // 3-1) 이번 주 동일 템플릿의 기존 배정 날짜들(중복 방지)
    Set<LocalDate> alreadyInWeek = taskAssignmentRepository.findByTemplate_Id(req.getTemplateId())
        .stream()
        .map(TaskAssignment::getDate)
        .filter(d -> !d.isBefore(weekStart) && !d.isAfter(weekEnd))
        .collect(Collectors.toSet());

    // 4) 담당자 선택
    boolean randomEnabled = Boolean.TRUE.equals(req.getRandomEnabled());
    Long pickedMemberId;
    if (randomEnabled) {
      // 랜덤 배정(주마다 체크박스)
      pickedMemberId = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    } else {
      // "담당 그대로" = 직전 주(weekStart 이전)의 가장 최근 배정 담당자 유지
      TaskAssignment last = taskAssignmentRepository
          .findTopByTemplate_IdAndDateLessThanOrderByDateDesc(req.getTemplateId(), weekStart);
      if (last != null) {
        pickedMemberId = last.getGroupMemberId();
      } else {
        // 첫 주 등 이전 이력이 없으면 후보에서 랜덤 fallback
        pickedMemberId = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
      }
    }

    // 5) 저장
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

  /** 목록 조회: 주간 스냅을 '일→토'로 통일 */
  public List<TaskAssignmentResponse> getAssignments(Long groupId, LocalDate from, LocalDate to, Long memberId) {
    if (groupId == null) throw new CustomException(ErrorCode.INVALID_REQUEST);

    LocalDate[] range = computeWeekRangeSunSat(from, to); // [Sun, Sat]
    LocalDate start = range[0], end = range[1];

    List<TaskAssignment> list = (memberId == null)
        ? taskAssignmentRepository.findByTemplate_GroupIdAndDateBetween(groupId, start, end)
        : taskAssignmentRepository.findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(groupId, memberId, start, end);

    return list.stream()
        .sorted(Comparator
            .comparing((TaskAssignment a) -> a.getDate().getDayOfWeek().getValue() % 7) // Sun=0
            .thenComparing(TaskAssignment::getDate)
            .thenComparing(TaskAssignment::getId))
        .map(a -> {
          String repeatType = repeatDayRepository.existsByTaskTemplate_Id(a.getTemplate().getId()) ? "WEEKLY" : "NONE";
          return TaskAssignmentResponse.from(a, repeatType);
        })
        .collect(Collectors.toList());
  }

  /** 주간 범위 계산: 일요일 시작 ~ 토요일 종료 */
  private static LocalDate[] computeWeekRangeSunSat(LocalDate from, LocalDate to) {
    ZoneId KST = ZoneId.of("Asia/Seoul");
    if (from == null && to == null) {
      LocalDate base = LocalDate.now(KST);
      LocalDate sun = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
      return new LocalDate[]{sun, sun.plusDays(6)};
    }
    if (from == null) from = to;
    if (to == null)   to   = from;
    if (to.isBefore(from)) { LocalDate tmp = from; from = to; to = tmp; }

    LocalDate start = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate end   = to.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    return new LocalDate[]{start, end};
  }
}
