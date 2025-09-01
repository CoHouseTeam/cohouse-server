package com.zero.cohousesever.group.dto.leaverequest;

import com.zero.cohousesever.group.enums.LeaveRequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LeaveRequestStatusDto {

    private LeaveRequestStatus status;
}
