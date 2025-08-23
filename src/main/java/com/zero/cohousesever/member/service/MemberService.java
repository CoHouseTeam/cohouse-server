package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.file.service.S3Service;
import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

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

    public MemberProfileImageResponseDto updateProfileImage(Long memberId, MultipartFile profileImage){
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(MEMBER_INACTIVE));

        if (member.getProfileImageUrl() != null) {
            String fileName = s3Service.extractFilePath(member.getProfileImageUrl());
            s3Service.deleteFile(fileName);
        }

        s3Service.validateImageFile(profileImage);

        String profileImageUrl;
        try {
            String dirName = String.format("members/%d", memberId);
            profileImageUrl = s3Service.uploadFile(profileImage, dirName);
        } catch (IOException e) {
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }

        member.updateProfileImageUrl(profileImageUrl);
        memberRepository.save(member);

        return MemberProfileImageResponseDto.builder()
                .imageUrl(profileImageUrl)
                .build();
    }
}
