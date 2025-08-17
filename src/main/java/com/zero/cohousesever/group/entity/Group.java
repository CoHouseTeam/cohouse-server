package com.zero.cohousesever.group.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.group.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "'group'") // 예약어 이슈 방지
public class Group extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    public void addMember(GroupMember member) {
        members.add(member);
        member.setGroup(this);
    }

    public void removeMember(GroupMember member) {
        members.remove(member);
        member.leaveGroup();
    }

    public void updateName(String newName) {
        this.name = newName;
    }
}

