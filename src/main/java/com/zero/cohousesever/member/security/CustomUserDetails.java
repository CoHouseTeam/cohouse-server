package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;
    @Getter
    private final String email;
    @Getter
    private final String name;
    private final String password;

    private final boolean isEnabled;

    public CustomUserDetails(Member member) {
        Objects.requireNonNull(member, "Member must not be null");

        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.password = member.getPassword();
        this.isEnabled = member.getStatus().equals(MemberStatus.ACTIVE);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 정보: 미사용
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료: 미사용
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠김: 미사용
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 비밀번호 만료일: 미사용
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성 상태: Member의 status가 ACTIVE인 경우
        return isEnabled;
    }
}
