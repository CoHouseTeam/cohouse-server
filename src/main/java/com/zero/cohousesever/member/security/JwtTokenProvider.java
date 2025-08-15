package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.enums.TokenValidationStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600 * 1000; // 1시간
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 24 * 3600 * 1000; // 24시간
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NAME = "name";


    // 설정에서 이슈어, 비밀키 설정하기. 비밀키는 BASE64 인코딩된 문자열 사용
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.secret}")
    private String secret;

    public String generateAccessToken(String email, String name) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .issuer(issuer)
                .claims()
                    .add(KEY_EMAIL, email)
                    .add(KEY_NAME, name)
                    .and()
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken() {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public TokenValidationStatus validateToken(String token) {
        try {
            parseClaims(token);
            return TokenValidationStatus.VALID;
        } catch (ExpiredJwtException e) {
            return TokenValidationStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationStatus.INVALID;
        }
    }

    private Claims parseClaims(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        JwtParser parser = Jwts.parser().verifyWith(secretKey).build();

        return parser.parseSignedClaims(token).getPayload();
    }

    // 유효하지 않은 토큰인 경우 null 반환
    public String getEmailFromToken(String token) {
        try {
            return parseClaims(token).get(KEY_EMAIL, String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get(KEY_EMAIL, String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
