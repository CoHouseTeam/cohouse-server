package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.AssignmentOverride;
import com.zero.cohousesever.tasks.entity.enums.OverrideStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentOverrideRepository extends JpaRepository<AssignmentOverride, Long> {

  List<AssignmentOverride> findByAssignment_Id(Long assignmentId);

  boolean existsByAssignment_IdAndTargetIdAndStatus(Long assignmentId, Long targetId, OverrideStatus status);

  List<AssignmentOverride> findAllByAssignment_IdAndStatus(Long assignmentId, OverrideStatus status);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
         update AssignmentOverride a
            set a.status = :toStatus,
                a.modifierId = :modifierId,
                a.respondedAt = CURRENT_TIMESTAMP
          where a.assignment.id = :assignmentId
            and a.status = :fromStatus
         """)
  int bulkUpdateStatusByAssignmentId(
      @Param("assignmentId") Long assignmentId,
      @Param("fromStatus") OverrideStatus fromStatus,
      @Param("toStatus") OverrideStatus toStatus,
      @Param("modifierId") Long modifierId
  );
}
