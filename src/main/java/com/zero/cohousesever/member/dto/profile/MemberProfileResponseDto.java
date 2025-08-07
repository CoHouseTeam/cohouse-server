package com.zero.cohousesever.member.dto.profile;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class MemberProfileResponseDto {

    private Long id;
    private String email;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String profileImageUrl;
    private LocalTime alertTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
