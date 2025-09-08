package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.file.service.S3Service;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.member.dto.profile.AlertTimeUpdateDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileSummary;
import com.zero.cohousesever.member.dto.profile.ProfileUpdateDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.Gender;
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

import static com.zero.cohousesever.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

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
                .gender(Gender.MALE)
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

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        assertThat(result.getGender()).isEqualTo(testMember.getGender().getDescription());
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
                .hasMessage(MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 프로필 수정 성공")
    void updateMemberProfile_Success() {
        // given
        Long memberId = 1L;
        LocalDate birthDate = LocalDate.of(2000, 12, 31);
        String gender = "여자";

        ProfileUpdateDto requestDto = new ProfileUpdateDto();
        ReflectionTestUtils.setField(requestDto, "birthDate", birthDate);
        ReflectionTestUtils.setField(requestDto, "gender", gender);

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MemberProfileSummary result = memberService.updateMemberProfile(memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testMember.getId());
        assertThat(result.getBirthDate()).isEqualTo(birthDate);
        assertThat(result.getGender()).isEqualTo(gender);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(memberRepository).save(testMember);
    }

    @Test
    @DisplayName("회원 프로필 수정시 이미 탈퇴한 회원일 경우 예외 발생")
    void updateMemberProfile_ThrowsException_WhenInactiveMember() {
        // given
        Long memberId = 1L;
        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);

        ProfileUpdateDto requestDto = new ProfileUpdateDto();
        ReflectionTestUtils.setField(requestDto, "birthDate", LocalDate.of(2000, 12, 31));
        ReflectionTestUtils.setField(requestDto, "gender", "여자");

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateMemberProfile(memberId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 알림 시간 수정 성공")
    void updateMemberAlertTime_Success() {
        // given
        Long memberId = 1L;
        LocalTime alertTime = LocalTime.of(18, 0, 0);

        AlertTimeUpdateDto requestDto = new AlertTimeUpdateDto();
        ReflectionTestUtils.setField(requestDto, "alertTime", alertTime);

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MemberProfileSummary result = memberService.updateMemberAlertTime(memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testMember.getId());
        assertThat(result.getAlertTime()).isEqualTo(alertTime);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(memberRepository).save(testMember);
    }

    @Test
    @DisplayName("회원 알림 시간 수정시 이미 탈퇴한 회원일 경우 예외 발생")
    void updateMemberAlertTime_ThrowsException_WhenInactiveMember() {
        // given
        Long memberId = 1L;
        LocalTime alertTime = LocalTime.of(18, 0, 0);

        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);

        AlertTimeUpdateDto requestDto = new AlertTimeUpdateDto();
        ReflectionTestUtils.setField(requestDto, "alertTime", alertTime);

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateMemberAlertTime(memberId, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 정상적으로 상태가 INACTIVE로 변경")
    void deleteMember_Success() {
        // given
        Long memberId = 1L;
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        memberService.deleteMember(memberId);

        // then
        assertThat(testMember.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(memberRepository).findById(memberId);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈퇴시 그룹에 속해있는 경우 예외 발생")
    void deleteMember_ThrowsException_WhenStillInGroup() {
        // given
        Long memberId = 1L;
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage("아직 그룹에 소속된 회원입니다.");

        verify(groupMemberRepository).existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE);
        verify(memberRepository, never()).findById(anyLong());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈시 존재하지 않는 회원이면 예외 발생")
    void deleteMember_ThrowsException_WhenMemberNotFound() {
        // given
        Long memberId = 999L;
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 회원을 찾을 수 없습니다."); // ErrorCode.MEMBER_NOT_FOUND 메시지

        verify(memberRepository).findById(memberId);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 탈퇴시 이미 비활성 상태인 경우 예외 발생")
    void deleteMember_ThrowsException_WhenAlreadyInactive() {
        // given
        Long memberId = 1L;
        ReflectionTestUtils.setField(testMember, "status", MemberStatus.INACTIVE);
        when(groupMemberRepository.existsByMemberIdAndStatus(memberId, GroupMemberStatus.ACTIVE))
                .thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));

        // when & then
        assertThatThrownBy(() -> memberService.deleteMember(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 탈퇴한 회원입니다.");

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 프로필 이미지 업데이트 성공 - 기존 이미지가 있는 경우")
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
    @DisplayName("회원 프로필 이미지 업데이트 성공 - 기존 이미지가 없는 경우")
    void updateProfileImage_Success_WithoutExistingImage() throws IOException {
        // given
        Long memberId = 2L;
        Member testMemberWithoutProfileImage = Member.builder()
                .name("테스트유저2")
                .email("test2@example.com")
                .password("encodedPassword")
                .gender(Gender.FEMALE)
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
    @DisplayName("회원 프로필 이미지 업데이트시 파일 검증에 실패하면 예외 발생")
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
        doThrow(new CustomException(INTERNAL_SERVER_ERROR))
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
    @DisplayName("회원 프로필 이미지 업데이트 실패 - 존재하지 않는 회원")
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
                .hasMessage(MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service, never()).validateImageFile(any());
        verify(s3Service, never()).uploadFile(any(), anyString());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원 프로필 이미지 업데이트 실패 - 파일 업로드 중 예외 발생")
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
                .hasMessage(INTERNAL_SERVER_ERROR.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).validateImageFile(mockFile);
        verify(s3Service).extractFilePath(oldImageUrl);
        verify(s3Service).uploadFile(mockFile, "members/1");

        // 업로드 실패 시 DB 저장과 기존 이미지 삭제는 실행되지 않아야 함
        verify(memberRepository, never()).save(any());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("회원 프로필 이미지 업데이트 - 기존 이미지 삭제 실패해도 성공 반환")
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

    @Test
    @DisplayName("회원 프로필 이미지 삭제 성공")
    void deleteProfileImage_Success() {
        // given
        Long memberId = 1L;
        String profileImageUrl = testMember.getProfileImageUrl();

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        when(s3Service.extractFilePath(profileImageUrl))
                .thenReturn("old-image-path");
        doNothing().when(s3Service).deleteFile("old-image-path");

        // when
        memberService.deleteMemberProfileImage(memberId);

        // then
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).extractFilePath(profileImageUrl);
        verify(s3Service).deleteFile("old-image-path");
    }

    @Test
    @DisplayName("회원 프로필 이미지 삭제 성공 - 기존 이미지가 없는 경우")
    void deleteMemberProfileImage_Success_WithoutImage() {
        // given
        Long memberId = 2L;
        Member testMemberWithoutProfileImage = Member.builder()
                .name("테스트유저2")
                .email("test2@example.com")
                .password("encodedPassword")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .alertTime(LocalTime.of(12, 0, 0))
                .profileImageUrl(null)
                .status(MemberStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testMemberWithoutProfileImage, "id", 2L);

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMemberWithoutProfileImage));

        // when
        memberService.deleteMemberProfileImage(memberId);

        // then
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service, never()).extractFilePath(anyString());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("회원 프로필 이미지 삭제 실패 - 존재하지 않는 회원")
    void deleteMemberProfileImage_ThrowsException_WhenMemberNotFound() {
        // given
        Long memberId = 999L;

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.deleteMemberProfileImage(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage(MEMBER_INACTIVE.getMessage());

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service, never()).extractFilePath(anyString());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("회원 프로필 이미지 삭제 실패 - S3 파일 삭제 중 예외 발생")
    void deleteMemberProfileImage_ThrowsException_WhenS3DeleteFails() {
        // given
        Long memberId = 1L;
        String profileImageUrl = testMember.getProfileImageUrl();
        String fileName = "old-image-path";

        when(memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE))
                .thenReturn(Optional.of(testMember));
        when(s3Service.extractFilePath(profileImageUrl))
                .thenReturn(fileName);
        doThrow(new RuntimeException("S3 삭제 실패"))
                .when(s3Service).deleteFile(fileName);

        // when & then
        memberService.deleteMemberProfileImage(memberId);

        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.ACTIVE);
        verify(s3Service).extractFilePath(profileImageUrl);
        verify(s3Service).deleteFile(fileName);
    }
}
