package com.zero.cohousesever.group.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.group.enums.GroupStatus;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "groups") // 예약어 이슈 방지
public class Group extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private GroupStatus status;

    @OneToMany
    private List<GroupMember> members;
}
