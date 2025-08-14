package com.zero.cohousesever.member.controller;

import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class ProfileController {

    private final MemberService memberService;

    // 회원 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<MemberProfileSummary> getProfile() {

        return ResponseEntity.ok().build();
    }

    // 회원 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<MemberProfileSummary> updateProfile(
            @RequestBody MemberProfileSummary requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 알림 발송 설정 시간 변경
    @PutMapping("/profile/alert-time")
    public ResponseEntity<MemberProfileSummary> updateAlertTime(
            @RequestBody MemberProfileSummary requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 회원 프로필 이미지 수정
    @PutMapping("/profile/profile-image")
    public ResponseEntity<MemberProfileImageResponseDto> updateProfileImage(
            @RequestPart("image") MultipartFile imageFile
    ) {

        return ResponseEntity.ok().build();
    }

    // 회원 프로필 이미지 삭제
    @DeleteMapping("/profile/profile-image")
    public ResponseEntity<Void> deleteProfileImage() {

        return ResponseEntity.noContent().build();
    }
}
