package com.zero.cohousesever.member.service.oauth2.impl;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.member.dto.auth.OAuthProfile;
import com.zero.cohousesever.member.service.oauth2.OAuth2ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NaverOAuth2Provider implements OAuth2ProviderService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth2.naver.client-id}")
    private String clientId;
    @Value("${oauth2.naver.client-secret}")
    private String clientSecret;

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String buildAuthorizeUrl(String redirectUri) {
        return "https://nid.naver.com/oauth2.0/authorize"
                + "&response_type=code"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + UUID.randomUUID();
    }

    @Override
    public OAuthProfile getProfile(String code, String state, String redirectUri) {
        // 1. 토큰 교환
        String tokenUrl = UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
                .toUriString();

        String accessToken;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);
            accessToken = (String) response.getBody().get("access_token");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH2_GET_TOKEN_FAIL);
        }

        // 2. 프로필 조회
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            // 네이버는 http 응답 맵 내에 "response" 키에 프로필 정보가 들어있음
            Map<String, Object> userInfo = (Map<String, Object>) response.getBody().get("response");

            return OAuthProfile.builder()
                    .id((String) userInfo.get("id"))
                    .email((String) userInfo.get("email"))
                    .name((String) userInfo.get("name"))
                    .build();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH2_GET_USERINFO_FAIL);
        }
    }
}
