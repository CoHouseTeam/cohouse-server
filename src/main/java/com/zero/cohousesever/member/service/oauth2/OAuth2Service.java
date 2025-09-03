package com.zero.cohousesever.member.service.oauth2;

import com.zero.cohousesever.member.dto.auth.OAuthProfile;
import com.zero.cohousesever.member.dto.auth.JwtTokenResponseDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.member.security.JwtTokenProvider;
import com.zero.cohousesever.member.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2ProviderFactory providerFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    public String buildAuthorizeUrl(String provider, String redirectUri) {
        return providerFactory.getProvider(provider).buildAuthorizeUrl(redirectUri);
    }

    public JwtTokenResponseDto exchangeCodeAndLogin(String provider, String code, String state, String redirectUri) {
        OAuthProfile profile = providerFactory.getProvider(provider).getProfile(code, state, redirectUri);

        Member member = memberRepository.findByEmail(profile.getEmail())
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .email(profile.getEmail())
                                .name(profile.getName())
                                .password(UUID.randomUUID().toString()) // 소셜 로그인시 랜덤 비밀번호를 설정
                                .status(MemberStatus.ACTIVE)
                                .build()
                ));

        String accessToken = jwtTokenProvider.generateAccessToken(member.getEmail(), member.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        refreshTokenService.saveRefreshToken(member.getId(), refreshToken, JwtTokenProvider.REFRESH_TOKEN_EXPIRATION_TIME);

        return JwtTokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}