package com.zero.cohousesever.member.service.oauth2;

import com.zero.cohousesever.member.dto.auth.OAuthProfile;

public interface OAuth2ProviderService {

    String getProvider();

    String buildAuthorizeUrl(String redirectUri);

    OAuthProfile getProfile(String code, String state, String redirectUri);
}
