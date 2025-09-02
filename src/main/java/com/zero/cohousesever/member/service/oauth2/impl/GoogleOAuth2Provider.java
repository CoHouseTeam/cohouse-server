package com.zero.cohousesever.member.service.oauth2.impl;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.dto.auth.OAuthProfile;
import com.zero.cohousesever.member.service.oauth2.OAuth2ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Provider implements OAuth2ProviderService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth2.google.client-id}")
    private String clientId;
    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String buildAuthorizeUrl(String redirectUri) {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=openid%20email%20profile";
    }

    @Override
    public OAuthProfile getProfile(String code, String state, String redirectUri) {
        // 1. 토큰 교환
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        String accessToken;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    new HttpEntity<>(params, defaultHeaders()),
                    Map.class
            );
            accessToken = (String) response.getBody().get("access_token");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH2_GET_TOKEN_FAIL);
        }

        // 2. 프로필 조회
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> userInfo = response.getBody();

            return OAuthProfile.builder()
                    .id((String) userInfo.get("id"))
                    .name((String) userInfo.get("name"))
                    .email((String) userInfo.get("email"))
                    .build();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH2_GET_USERINFO_FAIL);
        }
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
