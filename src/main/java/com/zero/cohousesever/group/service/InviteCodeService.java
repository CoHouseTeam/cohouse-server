package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    // 혼동을 피하기 위해 I와 1, O와 0은 미사용
    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String INVITE_CODE_KEY_PREFIX = "inviteCode:";
    private static final int CODE_LENGTH = 8;
    private static final long EXPIRE_HOURS = 3;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom random = new SecureRandom();

    public String generateInviteCode(Long groupId) {
        String code;
        String redisKey;

        do {
            code = generateCode();
            redisKey = INVITE_CODE_KEY_PREFIX + code;
        } while (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey)));

        redisTemplate.opsForValue().set(redisKey, groupId.toString(), EXPIRE_HOURS, TimeUnit.HOURS);

        return code;
    }

    public Long validateInviteCode(String code) {
        String groupId = redisTemplate.opsForValue().get(INVITE_CODE_KEY_PREFIX + code);
        if (groupId == null) {
            throw new CustomException(ErrorCode.INVITE_CODE_INVALID);
        }

        return Long.valueOf(groupId);
    }

    // 코드 생성기
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }

        return sb.toString();
    }
}
