package com.zero.cohousesever.member.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.common.utils.RandomCodeGenerator;
import com.zero.cohousesever.member.dto.auth.PasswordForgotRequestDto;
import com.zero.cohousesever.member.dto.auth.PasswordResetRequestDto;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.enums.MemberStatus;
import com.zero.cohousesever.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String PASSWORD_RESET_KEY_PREFIX = "password_reset:";
    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final int TOKEN_LENGTH = 32;

    private final MemberRepository memberRepository;
    private final MailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final RandomCodeGenerator codeGenerator;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendPasswordResetMail(PasswordForgotRequestDto requestDto) {
        memberRepository.findByEmailAndNameAndStatus(requestDto.getEmail(), requestDto.getName(), MemberStatus.ACTIVE)
                // 멤버가 존재하는 경우 이메일 발송
                // 멤버가 존재하지 않으면 메일 발송하지 않고 그대로 리턴
                .ifPresent(member -> {
                    String token = codeGenerator.generateCode(TOKEN_LENGTH);
                    String redisKey = PASSWORD_RESET_KEY_PREFIX + token;
                    redisTemplate.opsForValue().set(redisKey, member.getId().toString(), TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);

                    sendEmail(member.getEmail(), member.getName(), token);
                });
    }

    public void setNewPassword(PasswordResetRequestDto requestDto) {
        String redisKey = PASSWORD_RESET_KEY_PREFIX + requestDto.getToken();
        String memberId = redisTemplate.opsForValue().get(redisKey);

        if (memberId == null) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        Member member = memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        memberRepository.save(member);

        redisTemplate.delete(redisKey);

        log.info("비밀번호 재설정 완료: {}", member.getEmail());
    }

    private void sendEmail(String email, String name, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(senderEmail);
        message.setSubject("[CoHouse] 비밀번호 재설정 안내");

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String messageBody = String.format(
                """
                        안녕하세요 %s님,
                        
                        비밀번호 재설정을 요청하셨습니다.
                        아래 링크를 클릭하여 비밀번호를 재설정해 주세요.
                        
                        %s
                        
                        이 링크는 30분 후에 만료됩니다.
                        본인이 요청하지 않았다면 이 이메일을 무시해 주세요.
                        
                        감사합니다.
                        CoHouse 팀
                        """,
                name, resetUrl
        );
        message.setText(messageBody);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            // 이메일 전송 실패시 로그만 남기고 진행
            log.error("비밀번호 재설정 이메일 전송 실패: {} - {}", email, e.getMessage());
        }
    }
}
