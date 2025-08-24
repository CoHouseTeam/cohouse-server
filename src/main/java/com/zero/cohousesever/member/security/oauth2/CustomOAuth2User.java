package com.zero.cohousesever.member.security.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long memberId;
    private final String email;
    private final String name;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Long memberId, String email, String name, Map<String, Object> attributes) {
        this.memberId = memberId;
        this.email = email;
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // 권한 미사용으로 빈 리스트 반환
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return name;
    }

}
