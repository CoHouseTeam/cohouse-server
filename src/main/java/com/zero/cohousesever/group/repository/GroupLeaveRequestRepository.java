package com.zero.cohousesever.group.repository;

import com.zero.cohousesever.group.entity.GroupLeaveRequest;
import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupLeaveRequestRepository extends JpaRepository<GroupLeaveRequest, Long> {

    List<GroupLeaveRequest> findByGroupIdAndStatus(Long groupId, LeaveRequestStatus status);
}
