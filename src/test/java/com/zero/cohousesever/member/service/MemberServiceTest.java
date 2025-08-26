package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.file.service.S3Service;
import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 멤버 엔티티
        testMember = Member.builder()
                .name("테스트유저")
                .email("test@example.com")
                .password("encodedPassword")
                .gender(false) // male
                .birthDate(LocalDate.of(2000, 1, 1))
                .alertTime(LocalTime.of(12, 0, 0))
                .profileImageUrl("www.test.com/123")
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);
    }

    @Test
    @DisplayName("회원 생성 성공 - 정상적인 데이터로 회원 생성")
    void createMember_Success() {
        // given
        String name = "테스트유저";
        String email = "test@example.com";
        String encodedPassword = "encodedPassword";

        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // when
        Member result = memberService.createMember(name, email, encodedPassword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트유저");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 생성 시 저장되는 엔티티 검증")
    void createMember_VerifySavedEntity() {
        // given
        String name = "새로운유저";
        String email = "new@example.com";
        String encodedPassword = "newEncodedPassword";

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            return savedMember;
        });

        // when
        Member result = memberService.createMember(name, email, encodedPassword);

        // then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 프로필 조회 성공")
    void getMemberProfile_Success() {
        // given
        Long memberId = 1L;

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.of(testMember));

        // when
        MemberProfileSummary result = memberService.getMemberProfile(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testMember.getId());
        assertThat(result.getName()).isEqualTo(testMember.getName());
        assertThat(result.getEmail()).isEqualTo(testMember.getEmail());
        assertThat(result.getGender()).isEqualTo(testMember.getGender() ? "여자" : "남자");
        assertThat(result.getBirthDate()).isEqualTo(testMember.getBirthDate());
        assertThat(result.getAlertTime()).isEqualTo(testMember.getAlertTime());
        assertThat(result.getProfileImageUrl()).isEqualTo(testMember.getProfileImageUrl());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 프로필 조회시 이미 탈퇴한 회원일 경우 예외 발생")
    void getMemberProfile_ThrowsException_WhenInactiveMember() {
        // given
        Long memberId = 1L;
        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberProfile(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공 - 기존 이미지가 있는 경우")
    void updateProfileImage_Success_WithExistingImage() throws IOException {
        // given
        Long memberId = 1L;
        String oldImageUrl = testMember.getProfileImageUrl();
        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String newImageUrl = "https://s3.amazonaws.com/bucket/members/1/new-image.jpg";

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        doNothing().when(s3Service).validateImageFile(mockFile);
        when(s3Service.extractFilePath(oldImageUrl))
                .thenReturn("old-image-path");
        when(s3Service.uploadFile(mockFile, "members/1")).thenReturn(newImageUrl);
        doNothing().when(s3Service).deleteFile(anyString());
        when(memberRepository.save(testMember)).thenReturn(testMember);

        // when
        MemberProfileImageResponseDto result = memberService.updateProfileImage(memberId, mockFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(newImageUrl);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).validateImageFile(mockFile);
        verify(s3Service).extractFilePath(oldImageUrl);
        verify(s3Service).uploadFile(mockFile, "members/1");
        verify(s3Service).deleteFile("old-image-path");
        verify(memberRepository).save(testMember);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공 - 기존 이미지가 없는 경우")
    void updateProfileImage_Success_WithoutExistingImage() throws IOException {
        // given
        Long memberId = 2L;
        Member testMemberWithoutProfileImage = Member.builder()
                .name("테스트유저2")
                .email("test2@example.com")
                .password("encodedPassword")
                .gender(true) // female
                .birthDate(LocalDate.of(2000, 1, 1))
                .alertTime(LocalTime.of(12, 0, 0))
                .profileImageUrl(null)
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testMemberWithoutProfileImage, "id", 2L);

        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String newImageUrl = "https://s3.amazonaws.com/bucket/members/2/new-image.jpg";

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMemberWithoutProfileImage));
        doNothing().when(s3Service).validateImageFile(mockFile);
        when(s3Service.uploadFile(mockFile, "members/2")).thenReturn(newImageUrl);
        when(memberRepository.save(testMemberWithoutProfileImage)).thenReturn(testMemberWithoutProfileImage);

        // when
        MemberProfileImageResponseDto result = memberService.updateProfileImage(memberId, mockFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(newImageUrl);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).validateImageFile(mockFile);
        verify(s3Service, never()).extractFilePath(anyString());
        verify(s3Service).uploadFile(mockFile, "members/2");
        verify(s3Service, never()).deleteFile(anyString());
        verify(memberRepository).save(testMemberWithoutProfileImage);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트시 파일 검증에 실패하면 예외 발생")
    void updateProfileImage_ThrowsException_WhenValidationFails() throws IOException {
        // given
        Long memberId = 1L;
        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "not an image".getBytes()
        );

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        doThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR))
                .when(s3Service).validateImageFile(mockFile);

        // when & then
        assertThatThrownBy(() -> memberService.updateProfileImage(memberId, mockFile))
                .isInstanceOf(CustomException.class);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).validateImageFile(mockFile);

        // 검증 실패 시 다른 작업들은 실행되지 않아야 함
        verify(s3Service, never()).uploadFile(any(), anyString());
        verify(memberRepository, never()).save(any());
        verify(s3Service, never()).extractFilePath(anyString());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 실패 - 존재하지 않는 회원")
    void updateProfileImage_ThrowsException_WhenMemberNotFound() throws IOException {
        // given
        Long memberId = 999L;
        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateProfileImage(memberId, mockFile))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service, never()).validateImageFile(any());
        verify(s3Service, never()).uploadFile(any(), anyString());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 실패 - 파일 업로드 중 예외 발생")
    void updateProfileImage_ThrowsException_WhenUploadException() throws Exception {
        // given
        Long memberId = 1L;
        String oldImageUrl = testMember.getProfileImageUrl();
        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        doNothing().when(s3Service).validateImageFile(mockFile);
        when(s3Service.extractFilePath(oldImageUrl)).thenReturn("old-image-path");
        when(s3Service.uploadFile(mockFile, "members/1"))
                .thenThrow(new IOException("S3 업로드 실패"));

        // when & then
        assertThatThrownBy(() -> memberService.updateProfileImage(memberId, mockFile))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).validateImageFile(mockFile);
        verify(s3Service).extractFilePath(oldImageUrl);
        verify(s3Service).uploadFile(mockFile, "members/1");

        // 업로드 실패 시 DB 저장과 기존 이미지 삭제는 실행되지 않아야 함
        verify(memberRepository, never()).save(any());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 - 기존 이미지 삭제 실패해도 성공 반환")
    void updateProfileImage_Success_EvenWhenDeleteFails() throws Exception {
        // given
        Long memberId = 1L;
        String oldImageUrl = testMember.getProfileImageUrl();
        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String newImageUrl = "https://s3.amazonaws.com/bucket/members/1/new-image.jpg";

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        doNothing().when(s3Service).validateImageFile(mockFile);
        when(s3Service.uploadFile(mockFile, "members/1")).thenReturn(newImageUrl);
        when(s3Service.extractFilePath(oldImageUrl)).thenReturn("old-image-path");
        doThrow(new RuntimeException("S3 삭제 실패")).when(s3Service).deleteFile("old-image-path");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // when
        MemberProfileImageResponseDto result = memberService.updateProfileImage(memberId, mockFile);

        // then
        // 기존 이미지 삭제가 실패해도 메서드는 성공적으로 완료되어야 함
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isEqualTo(newImageUrl);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).validateImageFile(mockFile);
        verify(s3Service).extractFilePath(oldImageUrl);
        verify(s3Service).uploadFile(mockFile, "members/1");
        verify(memberRepository).save(testMember);
        verify(s3Service).deleteFile("old-image-path");
    }
}
