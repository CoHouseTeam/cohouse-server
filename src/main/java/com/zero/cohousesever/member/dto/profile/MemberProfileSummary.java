package com.zero.cohousesever.member.dto.profile;

import com.zero.cohousesever.member.entity.Member;
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

    private Long id;
    private String email;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String profileImageUrl;
    private LocalTime alertTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberProfileSummary fromEntity(Member member) {
        return MemberProfileSummary.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .gender(genderBooleanToString(member.getGender()))
                .birthDate(member.getBirthDate())
                .profileImageUrl(member.getProfileImageUrl())
                .alertTime(member.getAlertTime())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    public static String genderBooleanToString(Boolean gender) {
        return gender ? "여자" : "남자";
    }

    public static Boolean genderBooleanFromString(String gender) {
        return gender.equals("여자"); // 비정상 값에 대해 남자(false)를 기본값으로 사용
    }
}
