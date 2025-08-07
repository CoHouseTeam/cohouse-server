package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.TaskAssignment;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 특정 날짜에 특정 사용자에게 배정된 실제 할일 정보를 관리
 * 주간/월간 할일표 생성 시 사용예정
 */

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
  List<TaskAssignment> findByTaskTemplateId(Long templateId);
  List<TaskAssignment> findByAssignmentDateBetween(LocalDate start, LocalDate end);
}
