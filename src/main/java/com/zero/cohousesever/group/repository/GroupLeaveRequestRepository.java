package com.zero.cohousesever.group.repository;

import com.zero.cohousesever.group.entity.GroupLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupLeaveRequestRepository extends JpaRepository<GroupLeaveRequest, Long> {

}
