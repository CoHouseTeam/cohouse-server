package com.zero.cohousesever.member.controller;

import com.zero.cohousesever.member.dto.MessageDto;
import com.zero.cohousesever.member.dto.auth.*;
import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.member.service.AuthService;
import com.zero.cohousesever.member.service.MemberService;
import com.zero.cohousesever.member.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final PasswordResetService passwordResetService;

    // 이메일 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequestDto requestDto
    ) {
        authService.registerMember(requestDto);

        return ResponseEntity.ok().build();
    }

    // 회원가입 시 이메일 중복 체크
    @PostMapping("/check/email")
    public ResponseEntity<EmailDuplicateCheckResponseDto> checkEmailDuplicate(
            @RequestBody EmailDuplicateCheckRequestDto requestDto
    ) {
        boolean isDuplicated = authService.isEmailDuplicated(requestDto.getEmail());
        EmailDuplicateCheckResponseDto responseDto = EmailDuplicateCheckResponseDto.builder()
                .isDuplicate(isDuplicated)
                .build();

        return ResponseEntity.ok(responseDto);
    }

    // 이메일 로그인
    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        JwtTokenResponseDto responseDto = authService.loginAuthenticate(requestDto);

        return ResponseEntity.ok(responseDto);
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
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        authService.logout(memberId);

        return ResponseEntity.ok().build();
    }

    // 비밀번호 찾기 요청
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageDto> sendPasswordResetEmail(
            @RequestBody PasswordForgotRequestDto requestDto
    ) {
        passwordResetService.sendPasswordResetMail(requestDto);

        return ResponseEntity.ok().build();
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<MessageDto> resetPassword(
            @RequestBody PasswordResetRequestDto requestDto
    ) {
        passwordResetService.setNewPassword(requestDto);

        return ResponseEntity.ok().build();
    }

    // 리프레시 토큰을 통한 액세스 토큰 재발급
    @PostMapping("/login/refresh")
    public ResponseEntity<JwtTokenResponseDto> refreshAccessToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RefreshRequestDto requestDto
    ) {
        JwtTokenResponseDto responseDto = authService.reissueAccessToken(userDetails, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        memberService.deleteMember(memberId);

        return ResponseEntity.noContent().build();
    }
}