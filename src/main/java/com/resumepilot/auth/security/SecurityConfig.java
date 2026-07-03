package com.resumepilot.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumepilot.common.result.Result;
import com.resumepilot.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * 核心设计：
 *   1. 禁用 Session（用 JWT，不需要服务端存登录状态）
 *   2. 禁用 CSRF（前后端分离，不需要 Cookie 保护）
 *   3. 白名单放行：注册、登录、发验证码
 *   4. 其他接口都需要 JWT 认证
 *   5. 注册 JWT 过滤器到 Security 过滤器链
 *
 * 为什么要禁用 Session？
 *   JWT 是无状态认证，服务端不存 session
 *   如果开着 Session，Security 会尝试创建 HttpSession，浪费资源
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用 Session（用 JWT 无状态认证）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置哪些接口需要认证
            .authorizeHttpRequests(auth -> auth
                // 注册、登录、发验证码 → 不需要认证
                .requestMatchers("/auth/**").permitAll()
                // 静态资源放行
                .requestMatchers("/", "/index.html", "/*.js", "/*.css").permitAll()
                // 错误页面放行（避免 403 后转发 /error 再次被拦截）
                .requestMatchers("/error").permitAll()
                // 其他所有接口 → 需要 JWT 认证
                .anyRequest().authenticated()
            )
            // 401/403 直接返回 JSON，不转发到 /error
            .exceptionHandling(ex -> ex
                // 未登录 → 401
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                        new ObjectMapper().writeValueAsString(
                            Result.fail(ResultCode.UNAUTHORIZED, "未登录或 Token 已过期")
                        )
                    );
                })
                // 已登录但权限不足 → 403
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                        new ObjectMapper().writeValueAsString(
                            Result.fail(ResultCode.FORBIDDEN, "权限不足")
                        )
                    );
                })
            )
            // 在 UsernamePasswordAuthenticationFilter 之前执行 JWT 过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码加密器
     * BCrypt：每次加密同样的密码结果都不同（加了随机盐），但验证时能匹配
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
