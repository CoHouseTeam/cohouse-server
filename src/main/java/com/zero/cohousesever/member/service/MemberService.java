package com.zero.cohousesever.member.service;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(String name, String email, String encodedPassword) {
        Member newMember = Member.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .status(MemberStatus.ACTIVE)
                .build();

        return memberRepository.save(newMember);
    }
}
