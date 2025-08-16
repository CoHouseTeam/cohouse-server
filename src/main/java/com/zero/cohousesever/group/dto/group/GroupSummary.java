package com.zero.cohousesever.group.dto.group;

import com.zero.cohousesever.group.dto.groupmember.GroupMemberSummary;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.enums.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupSummary {

    private Long id;
    private String name;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GroupMemberSummary> groupMembers;

    public static GroupSummary fromEntity(Group group) {
        return GroupSummary.builder()
                .id(group.getId())
                .name(group.getName())
                .status(group.getStatus())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .groupMembers(
                        group.getMembers().stream()
                                .map(GroupMemberSummary::fromEntity)
                                .toList()
                )
                .build();
    }
}
