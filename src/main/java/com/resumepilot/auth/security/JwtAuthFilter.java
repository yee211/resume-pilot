package com.resumepilot.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 *
 * 每个请求都会经过这个过滤器（OncePerRequestFilter 保证只执行一次）
 *
 * 流程：
 *   1. 从 Header 取 Authorization: Bearer xxx
 *   2. 验证 token 是否有效
 *   3. 有效 → 把用户信息放进 SecurityContext（后续 Controller 可以拿到当前用户）
 *   4. 无效 → 不设置认证，Security 拦截返回 401
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                // 把用户放进 SecurityContext，后续可以通过
                // SecurityContextHolder.getContext().getAuthentication() 获取
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
