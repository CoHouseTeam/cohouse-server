package com.zero.cohousesever.member.security.oauth2;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            throw new CustomException(OAUTH2_USER_INFO_PROCESSING_FAILED);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new CustomException(OAUTH2_EMAIL_NOT_FOUND);
        }

        Member member = memberRepository.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> registerNewMember(oAuth2UserInfo));

        if (member.getStatus().equals(MemberStatus.INACTIVE)) {
            throw new CustomException(MEMBER_INACTIVE);
        }

        return new CustomOAuth2User(member.getId(), member.getEmail(), member.getName(), oAuth2User.getAttributes());
    }

    private Member registerNewMember(OAuth2UserInfo oAuth2UserInfo) {
        Member member = Member.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 임시 비밀번호
                .status(MemberStatus.ACTIVE)
                .build();

        return memberRepository.save(member);
    }
}
