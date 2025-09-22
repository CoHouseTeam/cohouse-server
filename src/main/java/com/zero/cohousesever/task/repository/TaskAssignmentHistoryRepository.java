package com.zero.cohousesever.task.repository;

import com.zero.cohousesever.task.entity.TaskAssignmentHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface TaskAssignmentHistoryRepository extends JpaRepository<TaskAssignmentHistory, Long> {

  List<TaskAssignmentHistory> findByAssignmentIdOrderByDateDescIdDesc(Long assignmentId);

  Optional<TaskAssignmentHistory> findByAssignmentIdAndDate(Long assignmentId, LocalDate date);

  @Query("""
       select h
         from TaskAssignmentHistory h
         join TaskAssignment a on a.id = h.assignmentId
        where (:groupId is null or a.template.groupId = :groupId)
          and (:memberId is null or h.groupMemberId = :memberId)
          and (:from is null or h.date >= :from)
          and (:to   is null or h.date <= :to)
        order by h.date desc, h.id desc
       """)
  List<TaskAssignmentHistory> searchByGroupMemberAndDateRange(
      @Param("groupId") Long groupId,
      @Param("memberId") Long memberId,
      @Param("from") java.time.LocalDate from,
      @Param("to")   java.time.LocalDate to
  );

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
    INSERT INTO tasks_assignment_histories
      (assignment_id, group_member_id, category, status, date, created_at, updated_at)
    VALUES
      (:assignmentId, :groupMemberId, :category, :status, :date, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
      group_member_id = VALUES(group_member_id),
      category       = VALUES(category),
      status         = VALUES(status),
      updated_at     = NOW()
  """, nativeQuery = true)
  void upsertHistory(
      @Param("assignmentId") Long assignmentId,
      @Param("groupMemberId") Long groupMemberId,
      @Param("category") String category,
      @Param("status") String status,
      @Param("date") java.sql.Date date
  );
}
