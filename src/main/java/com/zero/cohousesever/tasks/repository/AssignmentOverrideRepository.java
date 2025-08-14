package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.AssignmentOverride;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentOverrideRepository extends JpaRepository<AssignmentOverride, Long> {

  List<AssignmentOverride> findByAssignment_Id(Long assignmentId);
}
