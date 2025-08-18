package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.enums.TokenValidationStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final String secret = "testSecretKeyForJwtTokenProviderTestingPurposesOnly123456789012345678901234567890";
    private final String issuer = "test-issuer";
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "issuer", issuer);
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", secret);
    }

    @Test
    @DisplayName("JWT 액세스 토큰 생성 및 검증 성공")
    void shouldCreateAccessToken() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";

        // when
        String token = jwtTokenProvider.generateAccessToken(email, name);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 유효성 검증
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo(email);
        assertThat(getNameFromToken(token)).isEqualTo(name);
        assertThat(jwtTokenProvider.validateToken(token)).isEqualTo(TokenValidationStatus.VALID);
    }

    @Test
    @DisplayName("JWT 리프레시 토큰 생성 및 검증 성공")
    void shouldCreateRefreshToken() {
        // when
        String token = jwtTokenProvider.generateRefreshToken();

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 유효성 검증
        assertThat(jwtTokenProvider.validateToken(token)).isEqualTo(TokenValidationStatus.VALID);
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void shouldFailValidationForEmptyToken() {
        // given
        String emptyToken = "";

        // when
        TokenValidationStatus isValid = jwtTokenProvider.validateToken(emptyToken);

        // then
        assertThat(isValid).isEqualTo(TokenValidationStatus.INVALID);
    }

    @Test
    @DisplayName("null 토큰 검증 실패")
    void shouldFailValidationForNullToken() {
        // given
        String nullToken = null;

        // when
        TokenValidationStatus isValid = jwtTokenProvider.validateToken(nullToken);

        // then
        assertThat(isValid).isEqualTo(TokenValidationStatus.INVALID);
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void shouldFailValidationForExpiredToken() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";
        String expiredToken = generateExpiredToken(email, name);

        // when
        TokenValidationStatus isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isEqualTo(TokenValidationStatus.EXPIRED);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰 검증 실패")
    void shouldFailValidationForInvalidSecretKeyToken() {
        // given
        String email = "test@example.com";
        String name = "테스트 사용자";
        String invalidSecretKeyToken = generateInvalidSecretKeyToken(email, name);

        // when
        TokenValidationStatus isValid = jwtTokenProvider.validateToken(invalidSecretKeyToken);

        // then
        assertThat(isValid).isEqualTo(TokenValidationStatus.INVALID);
    }

    /**
     * 이하 테스트를 위한 헬퍼 메서드
     */

    // 만료된 토큰을 생성하는 메서드
    private String generateExpiredToken(String email, String name) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        Date now = new Date();
        // 만료 시간을 현재보다 과거로 설정 (예: 1초 전)
        Date expiration = new Date(now.getTime() - 1000);

        return Jwts.builder()
                .issuer(issuer)
                .claims()
                .add("email", email)
                .add("name", name)
                .and()
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // 다른 키로 토큰을 생성하는 메서드
    private String generateInvalidSecretKeyToken(String email, String name) {
        SecretKey secretKey = Keys.hmacShaKeyFor((secret+"test").getBytes());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000_000);

        return Jwts.builder()
                .issuer(issuer)
                .claims()
                .add("email", email)
                .add("name", name)
                .and()
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // 'name' 클레임을 가져오는 메서드
    private String getNameFromToken(String token) {
        return parseClaims(token).get("name", String.class);
    }

    private Claims parseClaims(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        JwtParser parser = Jwts.parser().verifyWith(secretKey).build();

        try {
            return parser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
