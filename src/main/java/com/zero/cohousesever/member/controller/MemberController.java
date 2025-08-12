package com.zero.cohousesever.member.controller;

import com.zero.cohousesever.member.dto.MessageDto;
import com.zero.cohousesever.member.dto.auth.*;
import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    // 이메일 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<MessageDto> signup(
            @RequestBody SignupRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 회원가입 시 이메일 중복 체크
    @PostMapping("/check/email")
    public ResponseEntity<EmailDuplicateCheckResponseDto> checkEmailDuplicate(
            @RequestBody EmailDuplicateCheckRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 이메일 로그인
    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDto> login(
            @RequestBody LoginRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 소셜 로그인 요청
    @GetMapping("/oauth2/{provider}")
    public ResponseEntity<JwtTokenResponseDto> oauth2Login(
            @PathVariable String provider
    ) {

        return ResponseEntity.ok().build();
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<MessageDto> logout() {

        return ResponseEntity.ok().build();
    }

    // 비밀번호 찾기 요청
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageDto> sendPasswordResetEmail(
            @RequestBody PasswordForgotRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<MessageDto> resetPassword(
            @RequestBody PasswordResetRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 리프레시 토큰을 통한 액세스 토큰 재발급
    @PostMapping("/login/refresh")
    public ResponseEntity<JwtTokenResponseDto> refreshAccessToken(
            @RequestBody RefreshRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw() {

        return ResponseEntity.ok().build();
    }

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
