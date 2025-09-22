package com.zero.cohousesever.member.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.enums.Gender;
import com.zero.cohousesever.member.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "members")
public class Member extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String profileImageUrl;

    private LocalDate birthDate;

    private LocalTime alertTime;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Convert(converter = GenderConverter.class)
    @Column(columnDefinition = "TINYINT")
    private Gender gender; // 0: Male, 1: Female

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfile(LocalDate birthDate, Gender gender) {
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (gender != null) {
            this.gender = gender;
        }
    }

    public void updateAlertTime(LocalTime alertTime) {
        this.alertTime = alertTime;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void withdraw() {
        this.status = MemberStatus.INACTIVE;
    }
}
