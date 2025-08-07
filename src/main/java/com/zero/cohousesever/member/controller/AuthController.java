package com.zero.cohousesever.member.controller;

import com.zero.cohousesever.member.dto.*;
import com.zero.cohousesever.member.dto.auth.EmailDuplicateCheckRequestDto;
import com.zero.cohousesever.member.dto.auth.EmailDuplicateCheckResponseDto;
import com.zero.cohousesever.member.dto.auth.LoginRequestDto;
import com.zero.cohousesever.member.dto.auth.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/signup")
    public ResponseEntity<MessageDto> signup() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageDto> logout() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/check/email")
    public ResponseEntity<EmailDuplicateCheckResponseDto> checkEmailDuplicate(
            @RequestBody EmailDuplicateCheckRequestDto requestDto
    ) {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageDto> sendPasswordResetEmail() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageDto> resetPassword() {

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw() {

        return ResponseEntity.ok().build();
    }

}
