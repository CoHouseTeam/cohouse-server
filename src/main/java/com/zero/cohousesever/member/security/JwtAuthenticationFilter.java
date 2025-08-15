package com.zero.cohousesever.member.security;

import com.zero.cohousesever.member.enums.TokenValidationStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            TokenValidationStatus status = jwtTokenProvider.validateToken(token);

            switch (status) {
                case VALID -> {
                    try {
                        String email = jwtTokenProvider.getEmailFromToken(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (RuntimeException e) { // TODO: userDetailsService.loadUserByUsername()에서 던지는 예외를 캐치하도록 변경
                        // 사용자를 찾지 못하면 인증 실패 처리
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // TODO: 적절한 예외 구현하기
                        return;
                    }
                }
                case EXPIRED -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // TODO: 적절한 예외 구현하기
                    return;
                }
                case INVALID -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // TODO: 적절한 예외 구현하기
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
