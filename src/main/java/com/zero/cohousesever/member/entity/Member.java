package com.zero.cohousesever.member.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import com.zero.cohousesever.member.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    private String name;

    private String profileImageUrl;

    private LocalDate birthDate;

    private LocalTime alertTime;

    private String email;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(columnDefinition = "TINYINT")
    private Integer gender; // 0: Male, 1: Female
}
