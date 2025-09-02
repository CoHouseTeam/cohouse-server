package com.zero.cohousesever.task.scheduler;

import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.task.entity.RepeatDay;
import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.repository.RepeatDayRepository;
import com.zero.cohousesever.task.repository.TaskAssignmentRepository;
import com.zero.cohousesever.task.repository.TaskTemplateRepository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskAssignmentScheduler {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayRepository repeatDayRepository;
  private final TaskAssignmentRepository taskAssignmentRepository;
  private final GroupMemberRepository groupMemberRepository;

  /** 매주 토요일 00:10에 Quartz/스케줄러에서 호출 (어노테이션은 Quartz 전환 시 제거됨) */
  @Transactional
  public void generateNextWeek() {
    LocalDate nextWeekAnchor = LocalDate.now(KST).plusWeeks(1);
    LocalDate sunday = nextWeekAnchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate weekFrom = sunday;
    LocalDate weekTo   = sunday.plusDays(6);

    List<TaskTemplate> templates = taskTemplateRepository.findAll().stream()
        .filter(t -> repeatDayRepository.existsByTaskTemplate_Id(t.getId()))
        .toList();

    for (TaskTemplate t : templates) {
      List<Long> candidates = groupMemberRepository.findMemberIdsByGroupIdAndStatus(
          t.getGroupId(), GroupMemberStatus.ACTIVE);
      if (candidates == null || candidates.isEmpty()) continue;

      Long assignee = resolveAssigneeForWeek(t, weekFrom, weekTo, candidates);

      Set<LocalDate> existingDates = taskAssignmentRepository
          .findByTemplate_IdAndDateBetween(t.getId(), weekFrom, weekTo)
          .stream().map(TaskAssignment::getDate).collect(Collectors.toSet());

      List<RepeatDay> rds = repeatDayRepository.findByTaskTemplate_Id(t.getId());
      List<TaskAssignment> toSave = new ArrayList<>();
      for (RepeatDay rd : rds) {
        LocalDate d = sunday.plusDays(rd.getDayOfWeek().getValue() % 7);
        if (existingDates.contains(d)) continue;
        toSave.add(TaskAssignment.builder()
            .template(t).groupMemberId(assignee).date(d).build());
      }
      if (!toSave.isEmpty()) taskAssignmentRepository.saveAll(toSave);
    }
  }

  /** ★ 신규 멤버 1순위 + 주간부하 최소 */
  private Long pickByPriority(Long groupId, LocalDate weekFrom, LocalDate weekTo, List<Long> candidates) {
    List<Long> tier1 = new ArrayList<>();
    List<Long> tier2 = new ArrayList<>();
    for (Long c : candidates) {
      boolean hasAnyHistory = taskAssignmentRepository.existsByTemplate_GroupIdAndGroupMemberId(groupId, c);
      if (!hasAnyHistory) tier1.add(c); else tier2.add(c);
    }
    List<Long> pool = !tier1.isEmpty() ? tier1 : tier2;

    var weeklyAll = taskAssignmentRepository.findByTemplate_GroupIdAndDateBetween(groupId, weekFrom, weekTo);
    Map<Long, Set<Long>> memberToTemplate = new HashMap<>();
    for (TaskAssignment a : weeklyAll) {
      Long m = a.getGroupMemberId();
      if (m == null) continue;
      memberToTemplate.computeIfAbsent(m, k -> new HashSet<>()).add(a.getTemplate().getId());
    }
    int best = Integer.MAX_VALUE;
    List<Long> tied = new ArrayList<>();
    for (Long c : pool) {
      int load = memberToTemplate.getOrDefault(c, Collections.emptySet()).size();
      if (load < best) { best = load; tied.clear(); tied.add(c); }
      else if (load == best) { tied.add(c); }
    }
    return tied.get(ThreadLocalRandom.current().nextInt(tied.size()));
  }

  /** ★ 랜덤일 때/직전담당자 유지 실패 시 → pickByPriority 사용 */
  private Long resolveAssigneeForWeek(TaskTemplate t, LocalDate weekFrom, LocalDate weekTo, List<Long> candidates) {
    if (t.isRandomEnabled()) {
      return pickByPriority(t.getGroupId(), weekFrom, weekTo, candidates);
    }
    TaskAssignment prev = taskAssignmentRepository
        .findTopByTemplate_IdAndDateLessThanOrderByDateDesc(t.getId(), weekFrom);
    if (prev != null && candidates.contains(prev.getGroupMemberId())) {
      return prev.getGroupMemberId();
    }
    return pickByPriority(t.getGroupId(), weekFrom, weekTo, candidates);
  }
}
