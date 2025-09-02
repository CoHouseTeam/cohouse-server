package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.AssignmentOverrideHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentOverrideHistoryRepository extends JpaRepository<AssignmentOverrideHistory, Long> {

  List<AssignmentOverrideHistory> findAllByOrderByRequestedAtDescIdDesc();

  List<AssignmentOverrideHistory> findByRequest_IdOrderByRespondedAtDescIdDesc(Long requestId);

}
