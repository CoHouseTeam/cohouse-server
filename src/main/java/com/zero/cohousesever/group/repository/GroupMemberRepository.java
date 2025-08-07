package com.zero.cohousesever.group.repository;

import com.zero.cohousesever.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

}
