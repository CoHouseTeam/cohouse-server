package com.zero.cohousesever.task.repository;

import com.zero.cohousesever.task.entity.TaskAssignment;
import com.zero.cohousesever.task.entity.enums.AssignmentStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

  // develop에서 온 메서드 (주간 중복 체크 등 기간 필터)
  List<TaskAssignment> findByTemplate_IdAndDateBetween(
      Long templateId, LocalDate start, LocalDate end);

  List<TaskAssignment> findByTemplate_GroupIdAndDateBetween(
      Long groupId, LocalDate start, LocalDate end);

  List<TaskAssignment> findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(
      Long groupId, Long groupMemberId, LocalDate start, LocalDate end);

  boolean existsByTemplate_GroupIdAndGroupMemberId(Long groupId, Long groupMemberId);

  // 기준일 이전의 가장 최근 배정 1건 (담당 그대로 유지용)
  TaskAssignment findTopByTemplate_IdAndDateLessThanOrderByDateDesc(Long templateId,
      LocalDate date);

  // 담당자 변경 시 동시성 제어
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select ta from TaskAssignment ta where ta.id = :id")
  Optional<TaskAssignment> findByIdForUpdate(@Param("id") Long id);

  List<TaskAssignment> findByTemplate_GroupIdAndDateBetweenAndStatusNot(
      Long groupId, LocalDate start, LocalDate end, AssignmentStatus status);

  List<TaskAssignment> findByTemplate_GroupIdAndGroupMemberIdAndDateBetweenAndStatusNot(
      Long groupId, Long groupMemberId, LocalDate start, LocalDate end, AssignmentStatus status);

  Optional<TaskAssignment> findByTemplate_IdAndDate(Long templateId, LocalDate date);

  // "해야할일" 조회용: SKIPPED 제외 + 활성 템플릿만 (템플릿 삭제 후 재 배정시 오류)
  List<TaskAssignment> findByTemplate_GroupIdAndDateBetweenAndStatusNotAndTemplate_ActiveTrue(
      Long groupId, LocalDate start, LocalDate end, AssignmentStatus status
  );

  List<TaskAssignment> findByTemplate_GroupIdAndGroupMemberIdAndDateBetweenAndStatusNotAndTemplate_ActiveTrue(
      Long groupId, Long groupMemberId, LocalDate start, LocalDate end, AssignmentStatus status);
}
