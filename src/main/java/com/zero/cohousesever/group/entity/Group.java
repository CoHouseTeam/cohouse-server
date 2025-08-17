package com.zero.cohousesever.group.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.group.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "groups")
public class Group extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private GroupStatus status;

    @OneToMany
    private List<GroupMember> members;
}

