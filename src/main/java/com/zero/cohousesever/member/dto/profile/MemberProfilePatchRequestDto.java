package com.zero.cohousesever.member.dto.profile;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MemberProfilePatchRequestDto {

    private String name;
    private LocalDate birthDate;
    private String gender;

}
