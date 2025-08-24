package com.zero.cohousesever.member.security.oauth2;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.security.oauth2.impl.GoogleOAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, final Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google" -> {
                return new GoogleOAuth2UserInfo(attributes);
            }

            /* 필요시 provider 추가 */

            default -> throw new CustomException(ErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
        }
    }
}
