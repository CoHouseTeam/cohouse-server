package com.zero.cohousesever.group.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.common.utils.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private static final String INVITE_CODE_KEY_PREFIX = "inviteCode:";
    private static final String INVITE_GROUP_KEY_PREFIX = "inviteGroupId:";
    private static final int CODE_LENGTH = 8;
    private static final long EXPIRE_HOURS = 3;

    private final StringRedisTemplate redisTemplate;
    private final RandomCodeGenerator codeGenerator;

    public String getInviteCodeByGroupId(Long groupId) {
        // 기존 코드가 있는지 조회
        String redisGroupKey = INVITE_GROUP_KEY_PREFIX + groupId;
        String existingCode = redisTemplate.opsForValue().get(redisGroupKey);

        // 기존 코드가 1시간 이상 남아있는 경우 TTL을 새로 갱신한 뒤 기존 코드 재사용
        if (existingCode != null) {
            String redisCodeKey = INVITE_CODE_KEY_PREFIX + existingCode;
            Long ttl = redisTemplate.getExpire(redisCodeKey, TimeUnit.MINUTES);

            if (ttl != null && ttl >= 60) {
                redisTemplate.expire(redisCodeKey, EXPIRE_HOURS, TimeUnit.HOURS);
                redisTemplate.expire(redisGroupKey, EXPIRE_HOURS, TimeUnit.HOURS);

                return existingCode;
            }
        }

        // 기존 코드가 없거나 1시간 미만으로 남았다면 새로운 코드 발급
        return generateInviteCode(groupId);
    }

    public Long getGroupIdByInviteCode(String code) {
        String groupId = redisTemplate.opsForValue().get(INVITE_CODE_KEY_PREFIX + code);
        if (groupId == null) {
            throw new CustomException(ErrorCode.INVITE_CODE_INVALID);
        }

        return Long.valueOf(groupId);
    }

    private String generateInviteCode(Long groupId) {
        String code = codeGenerator.generateCode(CODE_LENGTH);
        String redisCodeKey = INVITE_CODE_KEY_PREFIX + code;
        String redisGroupKey = INVITE_GROUP_KEY_PREFIX + groupId;

        // 코드->그룹 매핑과 그룹->코드 매핑을 같이 저장
        redisTemplate.opsForValue().set(redisCodeKey, groupId.toString(), EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(redisGroupKey, code, EXPIRE_HOURS, TimeUnit.HOURS);

        return code;
    }
}
