package com.zero.cohousesever.member.security;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.member.enums.TokenValidationStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.zero.cohousesever.common.exception.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static com.zero.cohousesever.common.exception.ErrorCode.ACCESS_TOKEN_INVALID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            TokenValidationStatus status = jwtTokenProvider.validateToken(token);

            if (status == TokenValidationStatus.VALID ||
                    (status == TokenValidationStatus.EXPIRED && request.getRequestURI().equals("/members/login/refresh"))) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (status == TokenValidationStatus.EXPIRED) {
                throw new CustomException(ACCESS_TOKEN_EXPIRED);
            } else if (status == TokenValidationStatus.INVALID) {
                throw new CustomException(ACCESS_TOKEN_INVALID);
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
