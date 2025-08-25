package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member createMember(String name, String email, String encodedPassword) {
        Member newMember = Member.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .status(MemberStatus.ACTIVE)
                .build();

        return memberRepository.save(newMember);
    }

    public MemberProfileSummary getMemberProfile(Long memberId) {
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(MEMBER_INACTIVE));

        return MemberProfileSummary.fromEntity(member);
    }
}
