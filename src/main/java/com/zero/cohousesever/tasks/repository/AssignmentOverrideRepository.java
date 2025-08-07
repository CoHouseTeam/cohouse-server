package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.AssignmentOverrideRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 기본 배정된 할일을 다른 사람에게 요청하거나 예외적으로 변경할 때 사용
 */

public interface AssignmentOverrideRepository extends JpaRepository<AssignmentOverrideRequest, Long> {
  List<AssignmentOverrideRequest> findByTaskAssignmentId(Long taskAssignmentId);
}
