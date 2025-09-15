package com.zero.cohousesever.member.dto.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileSummary {

    private static final String DEFAULT_PROFILE_URL = "https://cohouse-bucket.s3.ap-northeast-2.amazonaws.com/members/default/PersonCircle.png";

    private Long id;
    private String email;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String profileImageUrl;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime alertTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberProfileSummary fromEntity(Member member) {
        Gender gender = member.getGender();
        String profileImageUrl = member.getProfileImageUrl();

        return MemberProfileSummary.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .gender(gender != null ? gender.getDescription() : null)
                .birthDate(member.getBirthDate())
                .profileImageUrl(profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_URL)
                .alertTime(member.getAlertTime())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
