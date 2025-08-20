package com.zero.cohousesever.group.controller;

import com.zero.cohousesever.group.dto.group.GroupInviteUrlDto;
import com.zero.cohousesever.group.dto.group.GroupJoinDto;
import com.zero.cohousesever.group.dto.group.GroupNameDto;
import com.zero.cohousesever.group.dto.group.GroupSummary;
import com.zero.cohousesever.group.dto.groupmember.GroupMemberSummary;
import com.zero.cohousesever.group.dto.groupmember.LeaderTransferRequestDto;
import com.zero.cohousesever.group.dto.groupmember.LeaderTransferResponseDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestReasonDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestRespondDto;
import com.zero.cohousesever.group.dto.leaverequest.LeaveRequestSummary;
import com.zero.cohousesever.group.service.GroupService;
import com.zero.cohousesever.member.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // 그룹 생성
    @PostMapping
    public ResponseEntity<GroupSummary> createGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody GroupNameDto requestDto
    ) {
        Long memberId = userDetails.getId();

        GroupSummary responseDto = groupService.createGroup(memberId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 그룹 정보 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupSummary> getGroup(
            @PathVariable("groupId") Long groupId
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹 정보 수정
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupSummary> updateGroup(
            @PathVariable("groupId") Long groupId,
            @RequestBody GroupSummary requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹 해체
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable("groupId") Long groupId
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹 초대 링크 발급
    @PostMapping("/{groupId}/invitations")
    public ResponseEntity<GroupInviteUrlDto> createGroupInviteUrl(
            @PathVariable("groupId") Long groupId
    ) {

        return ResponseEntity.ok().build();
    }

    // 초대 링크로 그룹 가입
    @PostMapping("/join")
    public ResponseEntity<GroupMemberSummary> joinGroup(
            @RequestBody GroupJoinDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 내 그룹 정보 조회
    @GetMapping("/me")
    public ResponseEntity<GroupSummary> getMyGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getId();

        GroupSummary responseDto = groupService.getGroupByMemberID(memberId);

        return ResponseEntity.ok(responseDto);
    }

    // 그룹 멤버 목록 조회
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberSummary>> getGroupMemberList(
            @PathVariable("groupId") Long groupId
    ) {

        return ResponseEntity.ok().build();
    }

    // 멤버 상세 조회
    @GetMapping("/{groupId}/members/{groupMemberId}")
    public ResponseEntity<GroupMemberSummary> getGroupMember(
            @PathVariable Long groupId,
            @PathVariable("groupMemberId") Long groupMemberId
    ) {

        return ResponseEntity.ok().build();
    }

    // 본인의 그룹 멤버 정보 수정
    @PutMapping("/{groupId}/members/me")
    public ResponseEntity<GroupMemberSummary> updateGroupMember(
            @PathVariable Long groupId,
            @RequestBody GroupMemberSummary requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹장 권한 위임
    @PutMapping("/{groupId}/leader-transfer")
    public ResponseEntity<LeaderTransferResponseDto> changeGroupLeader(
            @PathVariable("groupId") Long groupId,
            @RequestBody LeaderTransferRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹 탈퇴 요청
    @GetMapping("/{groupId}/leave-requests")
    public ResponseEntity<List<LeaveRequestSummary>> getGroupLeaveRequests(
            @PathVariable("groupId") Long groupId
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹 탈퇴 요청
    @PostMapping("/{groupId}/leave-requests")
    public ResponseEntity<LeaveRequestSummary> createGroupLeaveRequests(
            @PathVariable("groupId") Long groupId,
            @RequestBody LeaveRequestReasonDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    // 그룹 탈퇴 승인(그룹장)
    @PostMapping("/{groupId}/leave-requests/{leaveRequestId}")
    public ResponseEntity<LeaveRequestSummary> approveGroupLeaveRequest(
            @PathVariable("groupId") Long groupId,
            @PathVariable("leaveRequestId") Long leaveRequestId,
            @RequestBody LeaveRequestRespondDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }


}
