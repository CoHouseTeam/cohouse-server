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
                .gender(member.getGender() != null ? member.getGender().getDescription() : null)
                .birthDate(member.getBirthDate())
                .profileImageUrl(member.getProfileImageUrl())
                .alertTime(member.getAlertTime())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
