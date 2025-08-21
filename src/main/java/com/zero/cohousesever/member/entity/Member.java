package com.zero.cohousesever.member.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(columnDefinition = "TINYINT")
    private Boolean gender; // 0: Male, 1: Female

    public void updateProfile(String name, LocalDate birthDate, Boolean gender) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
