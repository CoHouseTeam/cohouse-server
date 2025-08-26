package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.zero.cohousesever.common.exception.ErrorCode.MEMBER_INACTIVE;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;

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

    // soft delete 구현
    public void deleteMember(Long memberId) {
        // 소속된 그룹이 존재하는 경우
        if (groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.MEMBER_STILL_IN_GROUP);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.MEMBER_INACTIVE);
        }

        member.withdraw();
        memberRepository.save(member);
    }
}
