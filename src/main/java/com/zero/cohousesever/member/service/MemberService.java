package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.file.service.S3Service;
import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.zero.cohousesever.common.exception.ErrorCode.*;

@Slf4j
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

    public MemberProfileImageResponseDto updateProfileImage(Long memberId, MultipartFile profileImage) {
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(MEMBER_INACTIVE));

        s3Service.validateImageFile(profileImage);

        // 기존 이미지파일이 있다면 제거를 위해 추출
        String oldProfileImageUrl = member.getProfileImageUrl();
        String oldFileName = null;
        if (oldProfileImageUrl != null) {
            oldFileName = s3Service.extractFilePath(oldProfileImageUrl);
        }

        String profileImageUrl;
        try {
            String dirName = String.format("members/%d", memberId);
            profileImageUrl = s3Service.uploadFile(profileImage, dirName);
        } catch (Exception e) {
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }

        member.updateProfileImageUrl(profileImageUrl);
        memberRepository.save(member);

        // 새 이미지 등록 성공 후 기존 이미지 파일 삭제
        if (oldFileName != null) {
            try {
                s3Service.deleteFile(oldFileName);
            } catch (Exception e) {
                // 기존 이미지 삭제 실패는 로그만 남기고 진행
                log.error("프로필 이미지 파일 삭제 실패: {} - {}", oldFileName, e.getMessage());
            }
        }

        return MemberProfileImageResponseDto.builder()
                .imageUrl(profileImageUrl)
                .build();
    }
}
