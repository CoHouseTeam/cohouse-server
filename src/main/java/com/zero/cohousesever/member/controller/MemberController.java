package com.zero.cohousesever.member.controller;

import com.zero.cohousesever.member.dto.profile.AlertTimePatchRequestDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileImageResponseDto;
import com.zero.cohousesever.member.dto.profile.MemberProfilePatchRequestDto;
import com.zero.cohousesever.member.dto.profile.MemberProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/members/profile")
@RequiredArgsConstructor
public class MemberController {

    @GetMapping
    public ResponseEntity<MemberProfileResponseDto> getProfile() {

        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<MemberProfileResponseDto> updateProfile(
            @RequestBody MemberProfilePatchRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/alert-time")
    public ResponseEntity<MemberProfileResponseDto> updateAlertTime(
            @RequestBody AlertTimePatchRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile-image")
    public ResponseEntity<MemberProfileImageResponseDto> updateProfileImage(
            @RequestPart("image") MultipartFile imageFile
    ) {

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<Void> deleteProfileImage() {
        
        return ResponseEntity.noContent().build();
    }
}
