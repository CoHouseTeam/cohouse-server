package com.zero.cohousesever.member.entity;

import com.zero.cohousesever.member.enums.MemberStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String profileImageUrl;

    private LocalDate birthDate;

    private LocalTime alertTime;

    private String oauthId;

    private String email;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(columnDefinition = "TINYINT")
    private Integer gender; // 0: Male, 1: Female

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
