package com.zero.cohousesever.task.repository;

import com.zero.cohousesever.task.entity.TaskAssignmentHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssignmentHistoryRepository extends JpaRepository<TaskAssignmentHistory, Long> {

  List<TaskAssignmentHistory> findByAssignmentIdOrderByDateDescIdDesc(Long assignmentId);

  Optional<TaskAssignmentHistory> findByAssignmentIdAndDate(Long assignmentId, LocalDate date);

}
