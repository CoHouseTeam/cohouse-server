package com.zero.cohousesever.member.service;

import com.zero.cohousesever.member.dto.auth.JwtTokenResponseDto;
import com.zero.cohousesever.member.dto.auth.LoginRequestDto;
import com.zero.cohousesever.member.dto.auth.RefreshRequestDto;
import com.zero.cohousesever.member.dto.auth.SignupRequestDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.member.security.CustomUserDetails;
import com.zero.cohousesever.member.security.JwtTokenProvider;
import com.zero.cohousesever.member.security.TokenValidationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

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
        String passwordRepeat = signupRequestDto.getPasswordRepeat();

        // TODO: Validation 도입하여 처리하기
        if (!StringUtils.hasText(name)
                || !StringUtils.hasText(email)
                || !StringUtils.hasText(password)
                || !StringUtils.hasText(passwordRepeat)) {
            throw new RuntimeException(); // TODO: 적절한 예외 처리 로직 작성
        }

        if (!Objects.equals(password, passwordRepeat)) {
            throw new RuntimeException(); // TODO: 적절한 예외 처리 로직 작성
        }

        if (isEmailDuplicated(email)) {
            throw new RuntimeException(); // TODO: 적절한 예외 처리 로직 작성
        }

        return memberService.createMember(name, email, passwordEncoder.encode(password));
    }

    public JwtTokenResponseDto loginAuthenticate(LoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(); // TODO: 적절한 예외 처리

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new RuntimeException(); // TODO: 적절한 예외 처리
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
            throw new RuntimeException(); // TODO: '유효하지 않은 리프레시 토큰입니다' 예외 작성
        }

        if (!jwtTokenProvider.validateToken(serverRefreshToken).equals(TokenValidationStatus.VALID)) {
            throw new RuntimeException(); // TODO: '리프레시 토큰이 만료되었습니다.' 예외 작성
        }

        if (!Objects.equals(memberRefreshToken, serverRefreshToken)) {
            throw new RuntimeException(); // TODO: '유효하지 않은 리프레시 토큰입니다' 예외 작성
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails.getEmail(), userDetails.getName());

        return JwtTokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(serverRefreshToken)
                .build();
    }
}
