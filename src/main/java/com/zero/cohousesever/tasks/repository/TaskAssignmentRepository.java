package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.TaskAssignment;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

  List<TaskAssignment> findByTemplate_Id(Long templateId);

  List<TaskAssignment> findByTemplate_GroupIdAndDateBetween(Long groupId, LocalDate start, LocalDate end);

  List<TaskAssignment> findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(
      Long groupId, Long groupMemberId, LocalDate start, LocalDate end);

  // 기준일 이전의 가장 최근 배정 1건 (담당 그대로 유지용)
  TaskAssignment findTopByTemplate_IdAndDateLessThanOrderByDateDesc(Long templateId, LocalDate beforeDate);
}
