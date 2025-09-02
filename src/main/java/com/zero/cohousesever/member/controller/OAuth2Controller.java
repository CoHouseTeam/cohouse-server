package com.zero.cohousesever.member.controller;

import com.zero.cohousesever.member.dto.auth.JwtTokenResponseDto;
import com.zero.cohousesever.member.service.oauth2.OAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/members/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/{provider}/authorize")
    public void redirectToProvider(
            @PathVariable String provider,
            @RequestParam("redirectUri") String redirectUri,
            HttpServletResponse response
    ) throws IOException {
        String authUrl = oAuth2Service.buildAuthorizeUrl(provider, redirectUri);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/{provider}")
    public ResponseEntity<JwtTokenResponseDto> exchangeCode(
            @PathVariable String provider,
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam("redirectUri") String redirectUri
    ) {
        JwtTokenResponseDto tokenResponse = oAuth2Service.exchangeCodeAndLogin(provider, code, state, redirectUri);
        return ResponseEntity.ok(tokenResponse);
    }
}
