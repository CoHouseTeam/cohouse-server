package com.zero.cohousesever.group.dto.groupmember;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderTransferResponseDto {

    private Long previousLeaderId;
    private Long newLeaderId;
}
