package com.zero.cohousesever.member.service;

import com.zero.cohousesever.member.dto.auth.JwtTokenResponseDto;
import com.zero.cohousesever.member.dto.auth.LoginRequestDto;
import com.zero.cohousesever.member.dto.auth.SignupRequestDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.member.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberService memberService;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Member registerMember(SignupRequestDto signupRequestDto) {

        String name = signupRequestDto.getName();
        String email = signupRequestDto.getEmail();
        String password = signupRequestDto.getPassword();
        String passwordRepeat = signupRequestDto.getPasswordRepeat();

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
        String refreshToken = jwtTokenProvider.generateRefreshToken(); // TODO: Redis 서버에 리프레시토큰 저장

        return JwtTokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }
}
