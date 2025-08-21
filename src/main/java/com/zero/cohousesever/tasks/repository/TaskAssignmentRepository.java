package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.TaskAssignment;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

  List<TaskAssignment> findByTemplate_Id(Long templateId);

  List<TaskAssignment> findByTemplate_GroupIdAndDateBetween(Long groupId, LocalDate start,
      LocalDate end);

  List<TaskAssignment> findByTemplate_GroupIdAndGroupMemberIdAndDateBetween(
      Long groupId, Long groupMemberId, LocalDate start, LocalDate end);

}
