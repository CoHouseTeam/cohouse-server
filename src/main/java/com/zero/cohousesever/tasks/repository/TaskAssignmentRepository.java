package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.TaskAssignment;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

  List<TaskAssignment> findByTemplate_Id(Long templateId);

  List<TaskAssignment> findByDateBetween(LocalDate start, LocalDate end);

  boolean existsByTemplate_IdAndDate(Long templateId, LocalDate date);

}
