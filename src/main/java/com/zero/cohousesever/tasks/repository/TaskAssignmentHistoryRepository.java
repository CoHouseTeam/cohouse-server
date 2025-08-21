package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.TaskAssignmentHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssignmentHistoryRepository extends JpaRepository<TaskAssignmentHistory, Long> {

  // TODO: memberId등 구현 예정
  List<TaskAssignmentHistory> findByAssignmentIdOrderByDateDescIdDesc(Long assignmentId);

}
