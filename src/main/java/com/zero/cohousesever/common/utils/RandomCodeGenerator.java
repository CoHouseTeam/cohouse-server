package com.zero.cohousesever.common.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RandomCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateCode(int length) {
        // length 길이의 base64 문자열을 뽑기 위한 바이트 수 최적화
        int bytes = (int) Math.ceil(length * 6 / 8.0);
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);

        String s = Base64.getUrlEncoder().withoutPadding().encodeToString(buf);

        return s.substring(0, length);
    }
}
