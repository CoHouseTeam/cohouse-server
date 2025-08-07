package com.zero.cohousesever.member.dto.profile;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class AlertTimePatchRequestDto {

    private LocalTime alertTime;
}
