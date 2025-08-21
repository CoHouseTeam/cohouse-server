package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.dto.auth.JwtTokenResponseDto;
import com.zero.cohousesever.member.dto.auth.LoginRequestDto;
import com.zero.cohousesever.member.dto.auth.RefreshRequestDto;
import com.zero.cohousesever.member.dto.auth.SignupRequestDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.member.security.JwtTokenProvider;
import com.zero.cohousesever.member.enums.TokenValidationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Member registerMember(SignupRequestDto signupRequestDto) {

        String name = signupRequestDto.getName();
        String email = signupRequestDto.getEmail();
        String password = signupRequestDto.getPassword();

        // TODO: Validation 도입하여 처리하기
        if (!StringUtils.hasText(name)
                || !StringUtils.hasText(email)
                || !StringUtils.hasText(password)) {
            throw new CustomException(INVALID_SIGNUP_REQUEST);
        }

        if (isEmailDuplicated(email)) {
            throw new CustomException(EMAIL_ALREADY_EXISTS);
        }

        return memberService.createMember(name, email, passwordEncoder.encode(password));
    }

    public JwtTokenResponseDto loginAuthenticate(LoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new CustomException(PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getEmail(), member.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken();
        // 리프레시 토큰은 redis에 저장
        refreshTokenService.saveRefreshToken(member.getId(), refreshToken, JwtTokenProvider.REFRESH_TOKEN_EXPIRATION_TIME);

        return JwtTokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }

    public JwtTokenResponseDto reissueAccessToken(CustomUserDetails userDetails, RefreshRequestDto requestDto) {
        String memberRefreshToken = requestDto.getRefreshToken();
        String serverRefreshToken = refreshTokenService.getRefreshToken(userDetails.getId());

        if (!jwtTokenProvider.validateToken(memberRefreshToken).equals(TokenValidationStatus.VALID)) {
            throw new CustomException(REFRESH_TOKEN_INVALID);
        }

        if (!jwtTokenProvider.validateToken(serverRefreshToken).equals(TokenValidationStatus.VALID)) {
            throw new CustomException(REFRESH_TOKEN_EXPIRED);
        }

        if (!Objects.equals(memberRefreshToken, serverRefreshToken)) {
            throw new CustomException(REFRESH_TOKEN_INVALID);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails.getEmail(), userDetails.getName());

        return JwtTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(serverRefreshToken)
                .build();
    }
}
